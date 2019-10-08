// The MIT License
// SPDX short identifier: MIT
// Further resources on the MIT License
// Copyright 2018 Amit Thakur - amitthk - <e.amitthakur@gmail.com>
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

def getTimeStamp(){
    return sh (script: "date +'%Y%m%d%H%M%S%N' | sed 's/[0-9][0-9][0-9][0-9][0-9][0-9]\$//g'", returnStdout: true);
}
def getEnvVar(String paramName){
    return sh (script: "grep '${paramName}' target/classes/env_vars/project.properties|cut -d'=' -f2", returnStdout: true).trim();
}
def getTargetEnv(String branchName){
    def deploy_env = 'none';
    switch(branchName) {
        case 'master':
            deploy_env='uat'
        break
        case 'develop':
            deploy_env = 'dev'
        default:
            if(branchName.startsWith('release')){
                deploy_env='sit'
            }
            if(branchName.startsWith('feature')){
                deploy_env='none'
            }
    }
    return deploy_env
}

def getImageTag(String currentBranch)
{
    def image_tag = 'latest'
    if(currentBranch==null){
        image_tag = getEnvVar('IMAGE_TAG')
    }
    if(currentBranch=='master'){
        image_tag= getEnvVar('IMAGE_TAG')
    }
    if(currentBranch.startsWith('release')){
        image_tag = currentBranch.substring(8);
    }
    return image_tag
}
pipeline{

environment {
    GIT_COMMIT_SHORT_HASH = sh (script: "git rev-parse --short HEAD", returnStdout: true)
}

agent any

//tools {
//        maven 'Maven 3.3.9'
//        jdk 'jdk8'
//
//}

stages{
    stage ('Resources') {
          steps {
                script{
                    if(env.BRANCH_NAME=='master'){
                        sh 'mvn resources:resources -Dbranch.name=${BRANCH_NAME} -Dport.frontend=32000 -Dport.backend=31000 -Dport.queue=61616'
                    } else {
                        sh 'mvn resources:resources -Dbranch.name=${BRANCH_NAME} -Dport.frontend=32001 -Dport.backend=31001 -Dport.queue=61616'
                    }
                }
          }
    }
    stage('Init'){
        steps{
            //checkout scm;
        script{
        env.BASE_DIR = pwd()
        env.CURRENT_BRANCH = env.BRANCH_NAME
        sh 'echo "Branch_Name = ${BRANCH_NAME}"'
        env.IMAGE_TAG = getImageTag(env.CURRENT_BRANCH)
        sh 'echo "Image_Tag = ${IMAGE_TAG}"'
        env.TARGET_ENV = getTargetEnv(env.CURRENT_BRANCH)
        sh 'echo "Target_Env = ${TARGET_ENV}"'
        env.TIMESTAMP = getTimeStamp()
        env.APP_NAME= getEnvVar('APP_NAME')
        env.IMAGE_NAME = getEnvVar('IMAGE_NAME')
        env.PROJECT_NAME=getEnvVar('PROJECT_NAME')
        env.DOCKER_REGISTRY_URL=getEnvVar('DOCKER_REGISTRY_URL')
        env.RELEASE_TAG = getEnvVar('RELEASE_TAG')
        env.DOCKER_PROJECT_NAMESPACE = getEnvVar('DOCKER_PROJECT_NAMESPACE')
        env.DOCKER_IMAGE_TAG= "${DOCKER_REGISTRY_URL}/${DOCKER_PROJECT_NAMESPACE}/${APP_NAME}:${RELEASE_TAG}"
        env.JENKINS_DOCKER_CREDENTIALS_ID = getEnvVar('JENKINS_DOCKER_CREDENTIALS_ID')        
        env.JENKINS_GCLOUD_CRED_ID = getEnvVar('JENKINS_GCLOUD_CRED_ID')
        env.GCLOUD_PROJECT_ID = getEnvVar('GCLOUD_PROJECT_ID')
        env.GCLOUD_K8S_CLUSTER_NAME = getEnvVar('GCLOUD_K8S_CLUSTER_NAME')
        env.JENKINS_GCLOUD_CRED_LOCATION = getEnvVar('JENKINS_GCLOUD_CRED_LOCATION')

        }

        }
    }

    stage('Cleanup'){
        steps{
            sh '''
            docker rmi $(docker images -f 'dangling=true' -q) || true
            docker rmi $(docker images | sed 1,2d | awk '{print $3}') || true
            '''
        }

    }
    stage('Build'){
        steps{
            withEnv(["APP_NAME=${APP_NAME}", "PROJECT_NAME=${PROJECT_NAME}"]){
                sh '''
                cp ./target/classes/docker/Dockerfile .
                docker build -t ${DOCKER_REGISTRY_URL}/${DOCKER_PROJECT_NAMESPACE}/${IMAGE_NAME}:${RELEASE_TAG} --build-arg APP_NAME=${IMAGE_NAME}  -f Dockerfile .
                '''
            }
        }
    }
    stage('Publish'){
        steps{
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "${JENKINS_DOCKER_CREDENTIALS_ID}", usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWD']])
            {
            sh '''
            echo $DOCKER_PASSWD | docker login --username ${DOCKER_USERNAME} --password-stdin ${DOCKER_REGISTRY_URL}
            docker push ${DOCKER_REGISTRY_URL}/${DOCKER_PROJECT_NAMESPACE}/${IMAGE_NAME}:${RELEASE_TAG}
            docker logout
            '''
            }
        }
      }
    stage('Deploy'){
        steps{
        withCredentials([file(credentialsId: "${JENKINS_GCLOUD_CRED_ID}", variable: 'JENKINSGCLOUDCREDENTIAL')])
            {
            sh '''
                echo "====="
                echo ${JENKINSGCLOUDCREDENTIAL}
                echo "====="
                gcloud auth activate-service-account --key-file=/c/Users/jimsp/Downloads/pristine-surf-254112-e7fdcc81d8f3.json
                gcloud config set compute/zone europe-west2-a
                gcloud config set compute/region europe-west2
                gcloud config set project ${GCLOUD_PROJECT_ID}
                gcloud container clusters get-credentials ${GCLOUD_K8S_CLUSTER_NAME}

                echo "Having to undeploy deployments as I cant SED deployment files from my Jenkins due to permisssions"
                echo "kubectl delete deployment frontend"
                echo "kubectl delete deployment backend"
                echo "kubectl delete deployment queue"

                kubectl apply -f "$BASE_DIR"/target/classes/kube/deployment/
                kubectl rollout status --v=5 --watch=true -f "$BASE_DIR"/target/classes/kube/deployment/frontend-deployment.yaml

                gcloud auth revoke --all
                '''
            }
        }
    }

    stage('DeployToUAT'){
        steps{
//            input "Continue to Deploy to UAT?"
            sh '''
                echo "================"
                echo "DEPLOYING TO UAT"
                echo "================"
            '''
        }
    }
}

post {
    always {
        echo "Build# ${env.BUILD_NUMBER} - Job: ${env.JOB_NUMBER} status is: ${currentBuild.currentResult}"
        // emailext(attachLog: true,
        // mimeType: 'text/html',
        // body: '''
        // <h2>Build# ${env.BUILD_NUMBER} - Job: ${env.JOB_NUMBER} status is: ${currentBuild.currentResult}</h2>
        // <p>Check console output at &QUOT;<a href='${env.BUILD_URL'>${env.JOB_NAME} - [${env.BUILD_NUMBER}]</a>&QUOT;</p>
        // ''',
        // recipientProviders: [[$class: "FirstFailingBuildSusspectRecipientProvider"]],
        // subject: "Build# ${env.BUILD_NUMBER} - Job: ${env.JOB_NUMBER} status is: ${currentBuild.currentResult}",
        // to: "e.amitthakur@gmail.com")
    }
}
}