# TUI Technical Exercise

## Question 1 
### Overall Architecture & Components
The CI/CD pipeline is designed to automate the process of building, testing, and deploying applications. For this implementation, the pipeline uses:
- Version Control System: Git (Hosted on GitHub)
- Build System: GitHub Actions
- Containerization: Docker
- Deployment Target: AWS EC2

1. Code: The source code is pushed to the master branch on GitHub
2. Build & Test: The GitHub Actions Java CI Pipeline workflow will trigger the build (Maven) and test jobs (linter, unit tests, SAST, SCA)
3. Release: The GitHub Actions Docker Build and Push workflow will trigger the building of the Docker image which is then pushed into GHCR (GitHub Container Registry)
4. Deploy: The GitHub Actions Deploy to EC2 workflow will trigger the deployment of the Docker image. It will first pull from GHCR, then it will stop and remove the current container if it exists, and then it will run a new container
5. Monitor: The EC2 is monitored via AWS CloudWatch

### Ensuring Code Quality & Automated Testing
Version Control & Branch Protection
- GitHub branch protection rules ensure all changes go through pull requests 
- Before merging, the pull requests must pass the status check build-and-test from the Java CI Pipeline workflow
- Before merging, a code review approval is also required

Static Code Analysis
- A linter (Checkstyle) validates Java code style against a defined ruleset which by default is Google's Java style guide
- SonarQube reviews the code for security issues and issues with code quality
- SpotBugs acts as a second opinion. Detects common bugs and code smells
- OWASP Dependency-Check checks for the software's composition, its dependencies for any known vulnerabilities. It checks against the NVD database for CVSS scores

Automated Testing
- Unit tests run automatically via the GitHub Actions workflows
- Code coverage reports generated with JaCoCo to monitor test completeness
- XML and HTML reports are also generated for each of the static code analysis jobs and stored in the workflow artifacts
- Test failures block the pipeline, preventing broken code from being deployed

### Steps for Building and Deploying
Building
1. Checkout Source – Retrieves the latest code from GitHub
2. Set up JDK 17 – Prepares the Java build environment
3. Cache Maven Dependencies – Speeds up workflow execution
4. Run Checkstyle – Checks code against style rules and fails if violations are found
5. Run OWASP Dependency Check – Scans dependencies for known security vulnerabilities, failing the build if there are high severity (CVSS ≥ 7)
6. Run SpotBugs - Scans for potential bugs and code smells, fails if there are medium/high priority bugs
7. Run Tests with Coverage – Executes:
  - Unit tests
  - Creates test coverage reports (JaCoCo)
  - Packages the JAR (via mvn clean verify)
8. Run SonarQube Analysis – Performs code quality checks, security scans, and generates maintainability metrics
9. Upload Reports – Stores Checkstyle, SpotBugs, JaCoCo, and OWASP reports as artifacts
1o. Containerization - A Docker image is built containing the packaged JAR file and subsequently tags it and pushes it to GHCR

Deploying
1. Connect to EC2 - The pipeline will connect to EC2 via SSH using ip, key-pair and username stored in GitHub secrets
2. Pull from GHCR - It will pull the latest image from GHCR
3. Stop Container - It will stop the running container if it exists
4. Start New Container - It will start a new container, mapping port 80 on the server to port 8080 inside the container

### Benefits of the CI/CD Pipeline
1. Faster Feedback Loop - Automated builds and tests run on every push, allowing developers to detect and fix issues early
2. Improved Code Quality - Static code analysis, linting, and security scans ensure code quality and prevent vulnerabilities before deployment
3. Reliable Deployments - The same Docker image tested in CI is deployed to production, reducing environment related issues
4. Reduced Manual Effort - Building, testing, packaging, containerization, and deployment are fully automated allowing for more focus on development
5. Consistent Releases - The pipeline ensures that every release follows the same steps, producing predictable outcomes
6. Scalability - Using containers means the application can be easily deployed across environments


## Question 2
I've decided to use Terraform for this task, provisioning EC2, S3 and VPC resources and have uploaded the scripts under the folder terraform at the project root. I've decided to 
combine both Questions 1 and 2 whereby Question 1 deploys to the infrastructure I've provisioned in Question 2. Furthermore, to the Terraform configuration can be run via GitHub 
Action workflows manually. It will pull from the GitHub secrets for the key pair and the IP I restricted SSH access to. 

To give more details on the scripts, I provisioned the resources in the Singaporean region. The script creates a VPC, an EC2 with internet access and Docker installed, and an S3 bucket.

### Advantages of Using IaC for Infrastructure Provisioning and Maintenance
- Consistency: Ensures that your infrastructure can easily be duplicated across different environments, reducing errors and discrepancies that can happen with manually configuring resources
- Scalability: Eases the process of rapidly scaling infrastructure resources up or down based on demand. Iac allows you to easily allocate additional resources during peak times and release them when they are no longer needed 
- Efficiency: Reduces the time and effort required to provision and manage infrastructure. Routine tasks such as provisioning resources and patching servers can be automated
- Documentation: The scripts act as a single source of truth making it easier to track changes in the infrastructure

### How IaC Can Enhance Collaboration and Consistency in a DevOps Environment
- Documentation: Teams can work together more effectively, with clear visibility into changes and dependencies
- Consistency: Ensures that your infrastructure can easily be duplicated across different environments, reducing errors and discrepancies that can happen with manually configuring resources
- Efficiency: It is easier to onboard new team members as they can spin up environments without manual setup


## Question 3
### Issue Identification/Diagnosis
1. Firstly, you should collect information about the error. You can do so via the kubectl describe pod (pod name) > (text file name) command
2. You should then investigate the events section as it will provides additional context through events and pod status which includes errors. This can help with finding the root cause for the issue
3. After gathering information and investigating the events, identify the root cause of the issue and address it. For example, if it's a an Authorization Failed error, the issue
   is caused by using incorrect or expired credentials to access the container registry. To solve this issue, you should update the pod with the correct credentials or regenerate
   new credentials. Other than credentials, you'll also need to ensure that the pod has the proper permissions to access the container regsitry
4. Once you've attempted the fix, you can try to delete the pod and recreate it with the commands; kubectl delete pod (pod name) and kubectl apply -f (pod definition file). If
   the issue is resolved, the pod should start successfully

### Possible cause and resolution
There are numerous causes of this error. Here are a few common ones:
- Invalid name: Occurs if the image name specified in the pod is incorrect or does not exist in the container registry. To fix the issue, you should ensure that the image name is spelled correctly and that the image exists in the container registry before pulling it
- Missing or Incorrect Tags: Occurs if the image tag is missing or incorrect in the pod definition. To fix the issue, you must ensure that the tag is correctly specified during pod definition
- Invalid or Expired Credentials: As mentioned before, this is caused by using incorrect or expired credentials to access the container registry. To solve the issue, you should update the pod with the correct credentials or regenerate new credentials
- Network Connectivity Issues: Occurs when there are connectivity issues between the container registry and the Kubernetes cluster. To solve these issues, you should ensure there are no connectivity issues such as a slow connection, firewall blocking the connection or DNS issues
- Registry Rate Limit Issues: Occurs when your kubernetes cluster exceeds the rate limit allowed by the registry. Some container registries limit the amount of requests that can be made in a given time. To solve this issue, you must ensure the cluster does not exceed the rate limit. You can also use authenticated pulls as certain registries such as Docker Hub greatly limit unauthenticated pulls. Image mirroring and caching are other options that can be explored where more frequently used images are instead pulled from these sources instead 
