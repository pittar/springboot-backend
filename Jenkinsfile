try {
    def appName=env.APP_NAME
    def gitSourceUrl=env.GIT_SOURCE_URL
    def gitSourceRef=env.GIT_SOURCE_REF
    def project=""
    def projectVersion=""
    node("maven") {

        // Create new project for the feature branch if it does not exist
        String projectQuery = sh (
            script: 'oc get projects',
            returnStdout: true
        ).trim()

        stage("Initialize") {
            project = env.PROJECT_NAME
            echo "appName: ${appName}"
            echo "gitSourceUrl: ${gitSourceUrl}"
            echo "gitSourceRef: ${gitSourceRef}"

            openshift.withCluster() {
                openshift.withProject() {
                    if (!projectQuery.contains(appName)) {
                        stage ('Creating Project') {
                            print "Creating project ${appName} DEV and QA."
                            sh "oc new-project ${appName}-dev"
                            sh "oc new-project ${appName}-qa"

                            print "Give developers access to these projects."
                            sh "oc adm policy add-role-to-group view developer -n ${appName}-dev"
                            sh "oc adm policy add-role-to-group view developer -n ${appName}-qa"

                            print "Give new projects the ability to pull images from CI/CD."
                            sh "oc policy add-role-to-user system:image-puller system:serviceaccount:${appName}-dev:default -n cicd"
                            sh "oc policy add-role-to-user system:image-puller system:serviceaccount:${appName}-qa:default -n cicd"
                        }
                    }
                }
            }
        }
        stage("Checkout") {
            echo "Checkout source."
            git url: "${gitSourceUrl}", branch: "${gitSourceRef}"
            echo "Read POM info."
            pom = readMavenPom file: 'pom.xml'
            projectVersion = pom.version
        }
        stage("Build JAR") {
            echo "Build the app."
            sh "mvn clean package"
        }
        stage("Quality Check") {
   			sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=false"
            sh "mvn sonar:sonar -Dsonar.jacoco.reportPaths=target/coverage-reports/jacoco-unit.exec -Dsonar.host.url=http://sonarqube.cicd.svc:9000"
            sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeBom"
            //dependencyTrackPublisher(artifact: 'target/bom.xml', artifactType: 'bom', projectName: "${appName}", projectVersion: "${projectVersion}", synchronous: false)
        }
        stage("Build Image") {
            echo "Build container image."
            // unstash name:"jar"
            openshift.withCluster() {
                openshift.withProject('cicd') {
                    sh "oc start-build ${appName}-build --from-file=target/app.jar -n cicd --follow"
                }
            }
        }
        stage("Tag DEV") {
            echo "Tag image to DEV"
            openshift.withCluster() {
                openshift.withProject('cicd') {
                    openshift.tag("${appName}:latest", "${appName}:dev")
                }
            }
        }
        stage("Deploy DEV") {
            echo "Deploy to DEV."
            openshift.withCluster() {
                openshift.withProject("${appName}-dev") {
                    def deploymentsExists = openshift.selector( "dc", "app-backend").exists()
                    if (!deploymentsExists) {
                            echo "Deployments do not yet exist.  Create the environment."
                            def models = openshift.process( "cicd//app-backend-template", "-p", "IMAGE_TAG=dev" )
                            def created = openshift.create( models )
                    }
                    echo "Rollout to DEV."
                    def dc = openshift.selector('dc', "app-backend")
                    dc.rollout().latest()
                    dc.rollout().status()
                }
            }
        }
        stage("Tag for QA") {
            echo "Tag to UAT"
            openshift.withCluster() {
                openshift.withProject('cicd') {
                    openshift.tag("${appName}:dev", "${appName}:qa")
                }
            }
        }
        stage("Deploy QA") {
            echo "Deploy to QA"
            openshift.withCluster() {
                openshift.withProject("${appName}-qa") {
                    def deploymentsExists = openshift.selector( "dc", "app-backend").exists()
                    if (!deploymentsExists) {
                            echo "Deployments do not yet exist.  Create the environment."
                            def models = openshift.process( "cicd//app-backend-template", "-p", "IMAGE_TAG=qa" )
                            def created = openshift.create( models )
                    }
                    echo "Rollout to QA."
                    def dc = openshift.selector('dc', "app-backend")
                    dc.rollout().latest()
                    dc.rollout().status()
                }
            }
        }
    }
} catch (err) {
    echo "in catch block"
    echo "Caught: ${err}"
    currentBuild.result = 'FAILURE'
    throw err
}
