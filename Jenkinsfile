try {
    def appName=env.APP_NAME
    dev mavenMirror=env.MAVEN_MIRROR_URL
    def gitSourceUrl=env.GIT_SOURCE_URL
    def gitSourceRef=env.GIT_SOURCE_REF
    def project=""
    node {
        stage("Initialize") {
            project = env.PROJECT_NAME
            echo "appName: ${appName}"
            echo "gitSourceUrl: ${gitSourceUrl}"
            echo "gitSourceUrl: ${gitSourceUrl}"
            echo "gitSourceRef: ${gitSourceRef}"
        }
    }
    node("maven") {
        environment {
            MAVEN_MIRROR_URL = "${mavenMirror}"
        }
        stage("Checkout") {
            git url: "${gitSourceUrl}", branch: "${gitSourceRef}"
        }
        stage("Build JAR") {
            sh "mvn clean package"
            stash name:"jar", includes:"target/app.jar"
        }
    }
    node {
        stage("Build Image") {
            unstash name:"jar"
            sh "oc start-build ${appName}-build --from-file=target/app.jar -n ${project}"
        }
    }
} catch (err) {
    echo "in catch block"
    echo "Caught: ${err}"
    currentBuild.result = 'FAILURE'
    throw err
}
