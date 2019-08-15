try {
    def appName=env.APP_NAME
    def mavenMirror=env.MAVEN_MIRROR_URL
    def gitSourceUrl=env.GIT_SOURCE_URL
    def gitSourceRef=env.GIT_SOURCE_REF
    def project=""
    node("maven") {
        stage("Initialize") {
            project = env.PROJECT_NAME
            echo "appName: ${appName}"
            echo "mavenMirror: ${mavenMirror}"
            echo "gitSourceUrl: ${gitSourceUrl}"
            echo "gitSourceUrl: ${gitSourceUrl}"
            echo "gitSourceRef: ${gitSourceRef}"
        }
        stage("Checkout") {
            echo "Checkout source."
            git url: "${gitSourceUrl}", branch: "${gitSourceRef}"
        }
        stage("Build JAR") {
            echo "Build the app."
            sh "mvn clean package"
            stash name:"jar", includes:"target/app.jar"
        }
        stage("Quality Check") {
            sh "mvn sonar:sonar -Dsonar.host.url=http://sonarqube.cicd.svc:9000"
            sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeBom"
            dependencyTrackPublisher(artifact: 'target/bom.xml', artifactType: 'bom', projectName: "${appName}", synchronous: false)
        }
        stage("Build Image") {
            echo "Build container image."
            unstash name:"jar"
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
                openshift.withProject('app-dev') {
                    def deploymentsExists = openshift.selector( "dc", "${appName}").exists()
                    if (!deploymentsExists) {
                            echo "Deployments do not yet exist.  Create the environment."
                            def models = openshift.process( "cicd//demo-app-${appName}-template", "-p", "IMAGE_TAG=dev" )
                            def created = openshift.create( models )
                    }
                    echo "Rollout to DEV."
                    def dc = openshift.selector('dc', "${appName}")
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
                openshift.withProject('app-qa') {
                    def deploymentsExists = openshift.selector( "dc", "${appName}").exists()
                    if (!deploymentsExists) {
                            echo "Deployments do not yet exist.  Create the environment."
                            def models = openshift.process( "cicd//demo-app-${appName}-template", "-p", "IMAGE_TAG=qa" )
                            def created = openshift.create( models )
                    }
                    echo "Rollout to QA."
                    def dc = openshift.selector('dc', "${appName}")
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
