pipeline {
    agent {
        label "${SLAVE_NODE_IP}"
    }

    environment {
        // ANSI Colors for console output
        ANSI_RESET = "\u001B[0m"
        ANSI_RED = "\u001B[31m"
        ANSI_BLUE = "\033[34m"
        ANSI_PURPLE = "\033[35m"
        ANSI_GREEN = "\033[32m"
        ANSI_YELLOW = "\033[33m"
        FAILED_STAGE = ""

        // Server/Host related variables
        HOST_USER = "ubuntu"
        HOST_DIR = "/home"

        // Azure Storage related items
        AZURE_STORAGE_ACCOUNT = "abc"
        CONTAINER_NAME = "integration"
        AZURE_BUILD_DIR = ""

        // Azure SAS TOKEN RELATED VARIABLES
        SAS_KEY_DEV = "xxxxxx"
        SAS_KEY_INT = "yyyy"

        // Container Related items
        //HOST_STORAGE_DIR = "${HOST_DIR}/${HOST_USER}/${YOCTO_PROJECT_CONTAINER_NAME}"
        //CONTAINER_BASE_WORKDIR = "${HOST_STORAGE_DIR}"

        // Project directories
        IMX_YOCTO_DIR = "imx-project"
        YOCTO_BUILD_DIR = "build-wayland"
        
        // Meta layer directories
        META_ABCSERIRES_DIR = "meta-ABCSERIRES"
        META_HMI_DIR = "meta-hmi"
        META_FRAMEWORK_DIR = "meta-framework"
        META_POKY_DIR = "poky"
        META_FREESCALE_DIR = "meta-freescale"
        META_SWUPDATE_DIR = "meta-swupdate"
        META_CONNECTIVITY_DIR = "meta-connectivity"
        META_VERSION_AGGREGATOR_DIR = "meta-version-aggregator"
        META_IMAGE_OPTIMIZATION_DIR = "meta-image-optimization"
        META_SECURE_PART_DIR = "meta-secure-part"
        META_CONFIGURATION_DIR = "meta-configuration"
        META_TELEMATICS_DIR = "meta-telematics"
        WIFISERVICE_DIR = "wifiservice"

        // Additional project directories
        IMX_MKIMAGE_DIR = "imx-mkimage"
        IMX_MKIMAGE_REPO = "git@github.com:ABCSERIRES/imx-mkimage.git"
        IMX_MKIMAGE_TAG_OR_BRANCH = "release/Integration-Branch"

        // Build configuration
        BUILD_VARIANT = ""
        PLATFORM = "ABCSERIRES"
        PROJECT_NAME = "${JOB_BASE_NAME}"
        PROJECT_NUMBER = "${JOB_BASE_NAME}_Lynx"
        HMI_VARIANT = "ABCSERIRES"
        HMI_VEHICLEBRAND = "ABCSERIRES"
    }

    options {   
        timestamps()
        ansiColor('xterm')
        //disableConcurrentBuilds()
        timeout(time: 8, unit: 'HOURS')
        //buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        string(
            name: "ABCSERIRES_MANIFEST_PROJECT_URL",
            defaultValue: "git@github.com:ABCSERIRES/ABCSERIRES-manifest.git",
            description: 'ABCSERIRES_MANIFEST_PROJECT_URL (if not using manifest)'
        )
        string(
            name: "ABCSERIRES_MANIFEST_BRANCH_OR_TAG",
            defaultValue: "release/Integration-Branch",
            description: 'ABCSERIRES_MANIFEST_BRANCH_OR_TAG (if not using manifest)'
        )
        // Manifest selection (container will be auto-derived in Initialize stage)
        choice(
            name: 'ABCSERIRES_MANIFEST_FILE',
            choices: [
            'ABCSERIRES-manifest_abc_prod.xml',
            'ABCSERIRES-manifest_abc_dev.xml',
            'ABCSERIRES-manifest_bcd_dev.xml',
            'ABCSERIRES-manifest_cda_prod.xml',
            'ABCSERIRES-manifest_abc_dev.xml',
            'ABCSERIRES-manifest_bcd_prod.xml'
            ],
            description: 'Select manifest file (Docker container auto-selected)'
        )

        booleanParam(
            name: "CLEAN_REPO",
            defaultValue: false,
            description: "Clean existing .repo and sources directory before repo init (destructive)"
        )
        booleanParam(
            name: "CLEAN_BUILD",
            defaultValue: false,
            description: "Delete Yocto build tmp/cache directories before building"
        )
        booleanParam(
            name: "CLONE",
            defaultValue: true,
            description: "Enable/Disable CLONE stage"
        )
        booleanParam(
            name: "WIC_ImageGeneration",
            defaultValue: true,
            description: "Enable WIC image build stage"
        )
        booleanParam(
            name: "OTA_Generation",
            defaultValue: true,
            description: "Enable OTA (SWUpdate) image build stage"
        )
        booleanParam(
            name: "Bootloader_Generation",
            defaultValue: true,
            description: "Enable bootloader (flash.bin) generation stage"
        )
        booleanParam(
            name: "SDK_Generation",
            defaultValue: false,
            description: "Enable SDK generation stage"
        )
        booleanParam(
            name: "Artifacts_upload",
            defaultValue: true,
            description: "Enable upload of generated artifacts to Azure storage"
        )

        // Layer branch / tag parameters
        string(
            name: "META_ABCSERIRES",
            defaultValue: "",
            description: "Branch or tag for meta-ABCSERIRES layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_HMI",
            defaultValue: "",
            description: "Branch or tag for meta-hmi  (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_FRAMEWORK",
            defaultValue: "",
            description: "Branch or tag for meta-framework layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_CONNECTIVITY",
            defaultValue: "",
            description: "Branch or tag for meta-connectivity layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_TELEMATICS",
            defaultValue: "",
            description: "Branch or tag for meta-telematics layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "WIFISERVICE",
            defaultValue: "",
            description: "Branch or tag for WIFISERVICE layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_VERSION_AGGREGATOR",
            defaultValue: "",
            description: "Branch or tag for meta-version-aggregator layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_IMAGE_OPTIMIZATION",
            defaultValue: "",
            description: "Branch or tag for meta-image-optimization layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_CONFIGURATION",
            defaultValue: "",
            description: "Branch or tag for meta-configuration layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_SECURE_PART",
            defaultValue: "",
            description: "Branch or tag for meta-secure-part layer (default will be detected from manifest branch/tag)"
        )
        string(
            name: "META_SWUPDATE",
            defaultValue: "",
            description: "Branch or tag for meta-swupdate layer (default will be detected from manifest branch/tag)"
        )
        /*string(
            name: "PLATFORM",
            defaultValue: "ABCSERIRES",
            description: "Project name used in artifact paths and notifications"
        )
        string(
            name: "PROJECT_NAME",
            defaultValue: "ABCSERIRES",
            description: "Project name used in artifact paths and notifications"
        )
        string(
            name: "PROJECT_NUMBER",
            defaultValue: "ABCSERIRES_Lynx",
            description: "Project number / identifier"
        )
        choice(
            name: "HMI_VARIANT",
            choices: ['', 'ABCSERIRES', 'ABCSERIRES', 'ABCSERIRES'],
            description: "Select HMI Variant (Leave Blank If you are not sure about it)"
        )
        choice(
            name: "HMI_VEHICLEBRAND",
            choices: ['', 'ABC', 'ABCSERIRES', 'ABCSERIRES'],
            description: "Select Vehicle Brand (Leave Blank If you are not sure about it)"
        )*/
        choice(
            name: "CLOUD_ENV_INFO",
            choices: ['UAT', 'PROD', 'DEV', 'DEVOTA', 'QA'],
            description: "Select OTA Environment (will be written to meta-framework/recipes-vehicleinfo/ccmmanager/files/build_env.conf)"
        )
        string(
            name: "PLATFORM_OS_VERSION",
            defaultValue: "",
            description: "Full platform OS version in format: PREFIX-MJ.MN.MC-SUFFIX (Leave Blank If you are not sure about it)"
        )
        string(
            name: "OS_VERSION_VALUE",
            defaultValue: "",
            description: "OS Version in MJ.MN.MC format (e.g., 1.3.26) - Used as fallback if PLATFORM_OS_VERSION is not in correct format"
        )
        choice(
            name: "AZURE_CONTAINER_DIR",
            choices: ['internal', 'dev'],
            description: "Azure subdirectory inside container for upload"
        )
        // string(
        //     name: "YOCTO_PROJECT_CONTAINER_NAME",
        //     defaultValue: "jenkins_yocto_project_cda_dev",
        //     description: "Docker container name running Yocto build"
        // )
        string(
            name: "SLAVE_NODE_IP",
            defaultValue: "aws_build_servers",
            description: "Jenkins agent label / node IP, buildserver_35_aws, buildserver_18_aws, aws_build_servers"
        )
        booleanParam(
            name: "DJANGO_DATABASE_UPDATE",
            defaultValue: true,
            description: "Enable Django database update step"
        )
        choice(
            name: "database_create_instance",
            choices: """releasedetails_ABCSERIRES_create_instance
                        dailybuilds_ABCSERIRES_create_instance
                    """,
            description: """dailybuilds_ABCSERIRES_create_instance =====> Creates Database Entry in Dailybuilds
                            releasedetails_ABCSERIRES_create_instance =====> Creates Database Entry in Releasebuilds
                        """
        )
        string(
            name: "EMAIL_TO",
            defaultValue: "sw-release@example.com",
            description: "Comma-separated list of email recipients for build notifications(sw-release@example.com)"
        )

    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}=== ${STAGE_NAME} ===${ANSI_RESET}"

                        def buildNumber = "${BUILD_NUMBER}" as int
                        echo "BUILD_NUMBER: ${BUILD_NUMBER}"
                        if (buildNumber == 1) {
                            echo "${ANSI_YELLOW}First build detected, Intensionally exiting due to uninitialized variables${ANSI_RESET}"
                            exit 1
                        }
                        
                        // Optimize date commands - single call instead of multiple
                        def dateInfo = sh(script: """
                            NOW=\$(date +'%Y-%m-%d %H:%M')
                            TODAY=\$(date +'%Y-%m-%d')
                            YESTERDAY=\$(date -d 'yesterday' +'%Y-%m-%d')
                            DAY=\$(date +'%d')
                            MONTH=\$(date +'%m')
                            echo "\$NOW|\$TODAY|\$YESTERDAY|\$DAY|\$MONTH"
                        """, returnStdout: true).trim().split('\\|')

                        // Set global date variables
                        TODAY_DATE = dateInfo[0]
                        DATE = dateInfo[1]
                        YESTERDAY_DATE = dateInfo[2]
                        def currentDay = dateInfo[3]
                        def currentMonth = dateInfo[4]

                        // Store date variables in environment for persistence across stages
                        env.TODAY_DATE = TODAY_DATE
                        env.DATE = DATE
                        env.YESTERDAY_DATE = YESTERDAY_DATE

                        // Build directory names
                        SOFTWARE_RELEASE_BUILD_DIR = "${PROJECT_NAME}_${currentMonth}${currentDay}_#${BUILD_NUMBER}"
                        AWS_SOFTWARE_RELEASE_BUILD_DIR = "${PROJECT_NAME}_${currentMonth}${currentDay}_#${BUILD_NUMBER}"
                        AZURE_SOFTWARE_RELEASE_BUILD_DIR = "${PROJECT_NAME}_${currentMonth}${currentDay}_%23${BUILD_NUMBER}"
                        
                        // Store in environment for persistence across stages
                        env.SOFTWARE_RELEASE_BUILD_DIR = SOFTWARE_RELEASE_BUILD_DIR
                        env.AWS_SOFTWARE_RELEASE_BUILD_DIR = AWS_SOFTWARE_RELEASE_BUILD_DIR
                        env.AZURE_SOFTWARE_RELEASE_BUILD_DIR = AZURE_SOFTWARE_RELEASE_BUILD_DIR

                        // NOTE:
                        // Remove (or ignore) the existing YOCTO_PROJECT_CONTAINER_NAME parameter below.
                        // Add this snippet at the TOP of the Initialize stage (before initializeInfrastructure()):
                        //
                        def manifestToContainer = [
                            'ABCSERIRES-manifest_cda_dev.xml'   : 'jenkins_yocto_project_cda_dev',
                            'ABCSERIRES-manifest_abc_dev.xml'   : 'jenkins_yocto_project_abc_dev',
                            'ABCSERIRES-manifest_bcd_dev.xml'  : 'jenkins_yocto_project_bcd_dev',
                            'ABCSERIRES-manifest_cda_prod.xml'  : 'jenkins_yocto_project_cda_prod',
                            'ABCSERIRES-manifest_abc_prod.xml'  : 'jenkins_yocto_project_abc_prod',
                            'ABCSERIRES-manifest_bcd_prod.xml' : 'jenkins_yocto_project_bcd_prod'
                        ]
                        YOCTO_PROJECT_CONTAINER_NAME = manifestToContainer[ABCSERIRES_MANIFEST_FILE]
                        echo "Auto-selected container: ${YOCTO_PROJECT_CONTAINER_NAME}"
                        // Recompute HOST_STORAGE_DIR if it depends on container name:
                        HOST_STORAGE_DIR = "${HOST_DIR}/${HOST_USER}/${YOCTO_PROJECT_CONTAINER_NAME}"
                        CONTAINER_BASE_WORKDIR = "${HOST_STORAGE_DIR}"

                        // Initialize infrastructure
                        initializeInfrastructure()
                        
                        // Get system information
                        numOfCores = sh(returnStdout: true, script: 'nproc').trim()
                        echo "${ANSI_GREEN}Available CPU cores: ${numOfCores}${ANSI_RESET}"

                        // Use HTTPS for repository URL to avoid SSH resolution errors
                        def ABCSERIRESProjectUrlHttps = "${ABCSERIRES_MANIFEST_PROJECT_URL}".startsWith("git@github.com:") 
                            ? "${ABCSERIRES_MANIFEST_PROJECT_URL}".replace("git@github.com:", "git@xx.yy.xx.zz:").replace(".git", "") + ".git"
                            : "${ABCSERIRES_MANIFEST_PROJECT_URL}"

                        /*def ABCSERIRESProjectUrlHttps = "${ABCSERIRES_MANIFEST_PROJECT_URL}".startsWith("git@github.com:") 
                            ? "${ABCSERIRES_MANIFEST_PROJECT_URL}".replace("git@github.com:", "https://xx.yy.xx.zz/").replace(".git", "") + ".git"
                            : "${ABCSERIRES_MANIFEST_PROJECT_URL}"
                        */
                        // If credentials are needed for HTTPS, use git credential helper or pass via environment
                        // Example: set GIT_ASKPASS or use git config credential.helper
                        // For Jenkins, you can use withCredentials to inject username/password/token

                        // Store credentials in Jenkins using withCredentials
                        withCredentials([usernamePassword(credentialsId: 'narendra_github-https-creds', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                            // Configure git to use stored credentials for HTTPS
                            //sh '''
                            //    git config --global credential.helper store
                            //    echo "https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com" > ~/.git-credentials
                            //'''
                            // Determine if the ref is a tag or branch
                            // Retry logic for detecting whether the provided ref is a tag
                            int maxAttempts = 5
                            int delaySeconds = 5
                            def refType = ""
                            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                                try {
                                    refType = sh(
                                        script: """
                                            git ls-remote --tags '${ABCSERIRESProjectUrlHttps}' '${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}' 2>/dev/null | grep 'refs/tags/${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}' || true
                                        """,
                                        returnStdout: true
                                    ).trim()

                                    echo "RefType check output: '${refType}'"
                                    
                                    if (refType) {
                                        echo "${ANSI_GREEN}Tag '${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}' found (attempt ${attempt})${ANSI_RESET}"
                                        break
                                    } else {
                                        if (attempt < maxAttempts) {
                                            echo "${ANSI_YELLOW}Tag '${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}' not found (attempt ${attempt}/${maxAttempts}). Retrying in ${delaySeconds}s...${ANSI_RESET}"
                                            sleep time: delaySeconds, unit: 'SECONDS'
                                        } else {
                                            echo "${ANSI_BLUE}Assuming '${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}' is a branch after ${maxAttempts} attempts${ANSI_RESET}"
                                        }
                                    }
                                } catch (err) {
                                    // Rethrow if it's an abort (FlowInterruptedException)
                                    if (err.getClass().getName().contains("FlowInterruptedException") || err.getClass().getName().contains("InterruptedException")) {
                                        throw err
                                    }
                                    if (attempt < maxAttempts) {
                                        echo "${ANSI_YELLOW}Attempt ${attempt} failed (${err.message}). Retrying in ${delaySeconds}s...${ANSI_RESET}"
                                        sleep time: delaySeconds, unit: 'SECONDS'
                                    } else {
                                        echo "${ANSI_RED}All ${maxAttempts} attempts failed; proceeding assuming branch '${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}'${ANSI_RESET}"
                                    }
                                }
                            }
                            branchOrTag = refType ? "refs/tags/${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}" : "${ABCSERIRES_MANIFEST_BRANCH_OR_TAG}"
                        }

                        echo "${ANSI_GREEN}branchOrTag: ${branchOrTag}${ANSI_RESET}"
                        
                    } catch (Exception e) {
                        handleStageFailure(STAGE_NAME, e)
                    }
                }
            }
        }

        stage('CLONE') {
            when {
                expression { params.CLONE }
            }
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}=== ${STAGE_NAME} ===${ANSI_RESET}"
                        
                        // Setup container environment once
                        setupContainerEnvironment()
                        
                        // Initialize and sync repository
                        initializeRepository()
                        
                        // Handle branch checkouts
                        checkoutAllBranches()
                        
                        // Setup IMX mkimage
                        setupImxMkimage()
                        
                        // Extract project information
                        extractProjectInformation()
                        
                        // Update Platform OS version if requested
                        updatePlatformOsVersion()
                        
                        // Update CLOUD_ENV_INFO in build_env.conf
                        updateCloudEnvInfo()
                        
                    } catch (Exception e) {
                        handleStageFailure(STAGE_NAME, e)
                    }
                }
            }
        }

        stage('WIC Image Generation') {
            when {
                expression { params.WIC_ImageGeneration }
            }
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}=== ${STAGE_NAME} ===${ANSI_RESET}"
                        buildWicImage()
                    } catch (Exception e) {
                        handleStageFailure(STAGE_NAME, e)
                    }
                }
            }
        }
        
        stage('OTA Generation') {
            when {
                expression { params.OTA_Generation }
            }
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}=== ${STAGE_NAME} ===${ANSI_RESET}"
                        buildOtaImage()
                    } catch (Exception e) {
                        handleStageFailure(STAGE_NAME, e)
                    }
                }
            }
        }

        stage('Bootloader Generation') {
            when {
                expression { params.Bootloader_Generation }
            }
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}=== ${STAGE_NAME} ===${ANSI_RESET}"
                        buildBootloader()
                    } catch (Exception e) {
                        handleStageFailure(STAGE_NAME, e)
                    }
                }
            }
        }
        
        stage('Artifact Upload') {
            when {
                expression { params.Artifacts_upload }
            }
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}=== ${STAGE_NAME} ===${ANSI_RESET}"
                        uploadArtifacts()
                    } catch (Exception e) {
                        handleStageFailure(STAGE_NAME, e)
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                displayBuildSummary()
            }
        }
        success {
            echo "${ANSI_GREEN}✅ Build completed successfully!${ANSI_RESET}"
            script {
                def buildDir = safeGetVar('SOFTWARE_RELEASE_BUILD_DIR', "${PROJECT_NAME}_#${BUILD_NUMBER}")
                currentBuild.displayName = buildDir
                //build.displayName = "${PROJECT_NAME}_${PLATFORM_OS_VERSION_VALUE}_#${BUILD_NUMBER}"
                //AZURE_SOFTWARE_RELEASE_BUILD_DIR = "${PROJECT_NAME}_${PLATFORM_OS_VERSION}_%23${BUILD_NUMBER}"
                handleSuccess()
            }
        }
        failure {
            echo "${ANSI_RED}❌ Pipeline failed at stage: ${FAILED_STAGE}${ANSI_RESET}"
            script {
                handleFailure()
            }
        }
        cleanup {
            script {
                // Optional: Clean workspace if needed
                // cleanWs()
                echo "${ANSI_BLUE}Pipeline cleanup completed${ANSI_RESET}"
            }
        }
    }
}

// ========================================
// OPTIMIZED CORE FUNCTIONS
// ========================================

/**
 * Centralized error handling for all stages
 */
def handleStageFailure(stageName, exception) {
    // Check if it's an abort (FlowInterruptedException)
    if (exception.getClass().getName().contains("FlowInterruptedException") || exception.getClass().getName().contains("InterruptedException")) {
        echo "${ANSI_YELLOW}⛔ Aborted at stage: ${stageName}${ANSI_RESET}"
        throw exception
    }
    echo "${ANSI_RED}❌ FAILED: ${stageName} stage failed: ${exception.message}${ANSI_RESET}"
    currentBuild.result = 'FAILURE'
    FAILED_STAGE = stageName
    throw exception
}

/**
 * Initialize infrastructure components
 */
def initializeInfrastructure() {
    echo "${ANSI_BLUE}🔧 Initializing infrastructure...${ANSI_RESET}"
    
    // Create required directories
    def directories = [
        "${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}",
        "${HOST_STORAGE_DIR}/${IMX_MKIMAGE_DIR}"
    ]
    
    directories.each { dir ->
        checkAndCreateDirectory(dir)
    }
    
    // Start container with optimized check
    startContainerIfNotRunning("${YOCTO_PROJECT_CONTAINER_NAME}", "yoctoproject_dockerimage", "${CONTAINER_BASE_WORKDIR}")
    
    echo "${ANSI_GREEN}✅ Infrastructure initialized successfully${ANSI_RESET}"
}

/**
 * Setup container environment (avoid repetition)
 */
def setupContainerEnvironment() {
    echo "${ANSI_BLUE}🐳 Setting up container environment...${ANSI_RESET}"
    
    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        echo "Setting up container environment..."
        
        # Update package list and install required tools
        sudo apt update -qq
        sudo apt install -y git-lfs
        
        # Configure git globally once
        git config --global user.name "Jenkins"
        git config --global user.email "jenkins@example.com"
        
        # Navigate to working directory
        cd ${IMX_YOCTO_DIR}
        pwd
        
        echo "Container environment setup complete"
    """)
    
    echo "${ANSI_GREEN}✅ Container environment ready${ANSI_RESET}"
}

/**
 * Repository initialization with optimization
 */
def initializeRepository() {
    echo "${ANSI_BLUE}📦 Initializing repository...${ANSI_RESET}"
    
    // Clean repository if requested
    if (params.CLEAN_REPO == true) {
        echo "${ANSI_YELLOW}🧹 Cleaning repository for fresh clone...${ANSI_RESET}"
        dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
            set -xe
            cd ${IMX_YOCTO_DIR}
            echo "Cleaning repository for fresh clone..."
            rm -rf .repo sources
        """)
    }
    
    // Handle existing local branches
    handleExistingBranches()
    
    // Initialize and sync repository
    

    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        cd ${IMX_YOCTO_DIR}
        
        echo "Initializing repo..."

        repo init --repo-url=git@github.com:ABCSERIRES/git-repo.git -u ${ABCSERIRES_MANIFEST_PROJECT_URL} -b ${branchOrTag} -m ${ABCSERIRES_MANIFEST_FILE}

        echo "WARNING: Removing ALL local changes (reset and clean) - this is DESTRUCTIVE and intended for CI only!"
        repo forall -c "git reset --hard && git clean -fdx"
        
        echo "Syncing repository with ${numOfCores} parallel jobs..."
        repo sync -j${numOfCores} --force-sync --no-clone-bundle
        
        echo "Handling LFS files..."
        repo forall -c "git lfs pull 2>/dev/null || echo No LFS files in this repo"
        
        echo "Final sync to ensure consistency..."
        repo sync -j${numOfCores}
        
        echo "Repository initialization complete"
    """)
    
    echo "${ANSI_GREEN}✅ Repository initialized successfully${ANSI_RESET}"
}

/**
 * Handle existing local branches
 */
def handleExistingBranches() {
    dir("${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}") {
        try {
            dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
                set -xe
                cd ${IMX_YOCTO_DIR}
                
                echo "Detecting existing repo branches..."
                repo branches || true
                
                echo "Discarding any local (uncommitted) changes before abandoning branches..."
                repo forall -c '
                    if [ -n "\$(git status --porcelain)" ]; then
                        echo "Resetting \$REPO_PROJECT (\$REPO_PATH)"
                        git reset --hard
                        git clean -fd
                    fi
                '
                
                echo "Attempting to abandon all local topic branches..."
                if ! repo abandon --all; then
                    echo "repo abandon reported errors; performing aggressive cleanup fallback..."
                    repo forall -c 'git reset --hard && git clean -fd'
                fi
                
                echo "Verifying workspace is clean after abandon..."
                repo forall -c '
                    if [ -n "\$(git status --porcelain)" ]; then
                        echo "WARNING: Residual changes remain in \$REPO_PROJECT"
                    fi
                '
                
                echo "✅ Existing branches handled"
            """)
        } catch (Exception e) {
            // Rethrow if it's an abort (FlowInterruptedException)
            if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
                throw e
            }
            echo "${ANSI_YELLOW}⚠️ Warning: Could not process existing branches: ${e.message}${ANSI_RESET}"
        }
    }
}

/**
 * Optimized branch checkout for all components
 */
def checkoutAllBranches() {
    echo "${ANSI_BLUE}🌿 Checking out branches...${ANSI_RESET}"
    
    def branchConfigs = [
        [dir: META_ABCSERIRES_DIR, branch: META_ABCSERIRES, name: "META_ABCSERIRES"],
        [dir: META_HMI_DIR, branch: META_HMI, name: "META_HMI"],
        [dir: META_FRAMEWORK_DIR, branch: META_FRAMEWORK, name: "META_FRAMEWORK"],
        [dir: META_CONNECTIVITY_DIR, branch: META_CONNECTIVITY, name: "META_CONNECTIVITY"],
        [dir: META_CONFIGURATION_DIR, branch: META_CONFIGURATION, name: "META_CONFIGURATION"],
        [dir: META_VERSION_AGGREGATOR_DIR, branch: META_VERSION_AGGREGATOR, name: "META_VERSION_AGGREGATOR"],
        [dir: META_IMAGE_OPTIMIZATION_DIR, branch: META_IMAGE_OPTIMIZATION, name: "META_IMAGE_OPTIMIZATION"],
        [dir: META_SWUPDATE_DIR, branch: META_SWUPDATE, name: "META_SWUPDATE"],
        [dir: META_SECURE_PART_DIR, branch: META_SECURE_PART, name: "META_SECURE_PART"],
        [dir: META_TELEMATICS_DIR, branch: META_TELEMATICS, name: "META_TELEMATICS"],
        [dir: WIFISERVICE_DIR, branch: WIFISERVICE, name: "WIFISERVICE"]
    ]
    
    branchConfigs.each { config ->
        if (config.branch?.trim()) {
            echo "${ANSI_BLUE}Checking out ${config.name}: ${config.branch}${ANSI_RESET}"
            checkoutBranchOptimized(config.dir, config.branch)
        } else {
            echo "${ANSI_YELLOW}Skipping ${config.name} - no branch specified${ANSI_RESET}"
        }
    }
    
    echo "${ANSI_GREEN}✅ All branches checked out successfully${ANSI_RESET}"
}

/**
 * Optimized branch checkout with error handling
 */
def checkoutBranchOptimized(projectPath, branch) {
    dir("${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/sources/${projectPath}") {
        sh """
            set -xe
            echo "Checking out branch '${branch}' for ${projectPath}"

            # Fetch latest changes
            #git fetch --all --prune

            # Checkout branch
            git checkout ${branch}
            
            # Pull latest if it's a branch (not a tag/commit)
            if git show-ref --verify --quiet refs/heads/${branch} 2>/dev/null; then
                echo "Pulling latest changes for branch ${branch}"
                git pull ABCSERIRES ${branch} || echo "Pull not needed or failed (possibly a tag/commit)"
            else
                echo "${branch} is a tag or commit, skipping pull"
            fi
            
            # Handle LFS files if present
            git lfs pull 2>/dev/null || echo "No LFS files to pull"
            
            echo "✅ Successfully checked out ${branch}"
        """
    }
}

/**
 * Updates Platform OS version in versions.json and sets global variables for artifact naming.
 * Computes PLATFORM_OS_VERSION_VALUE in Jenkins scope so it's available to all stages.
 * Also extracts major, minor, micro version numbers from the version string.
 */
def updatePlatformOsVersion() {
    // Compute date values in Groovy scope
    def formattedDate = sh(script: "date +%d.%m.%y", returnStdout: true).trim()
    def currentDay = sh(script: "date +%d", returnStdout: true).trim()
    def currentMonth = sh(script: "date +%m", returnStdout: true).trim()
    
    // Regex pattern for valid PLATFORM_OS_VERSION format: PREFIX-MJ.MN.MC-SUFFIX (e.g., ABCSERIRES-03.04.26-1521)
    def validPlatformOsPattern = /^[A-Za-z0-9]+-\d+\.\d+\.\d+-.+$/
    
    // Regex pattern for valid OS_VERSION_VALUE format: MJ.MN.MC (e.g., 1.3.26)
    def validOsVersionPattern = /^\d+\.\d+\.\d+$/
    
    // Check if PLATFORM_OS_VERSION is provided and in correct format
    if (params.PLATFORM_OS_VERSION?.trim()) {
        def platformOsInput = params.PLATFORM_OS_VERSION.trim()
        
        if (platformOsInput ==~ validPlatformOsPattern) {
            // PLATFORM_OS_VERSION is in correct format (e.g., ABCSERIRES-03.04.26-1521)
            PLATFORM_OS_VERSION_VALUE = platformOsInput
            echo "${ANSI_GREEN}Using provided PLATFORM_OS_VERSION (valid format): ${PLATFORM_OS_VERSION_VALUE}${ANSI_RESET}"
        
        // Extract version between first and second "-" (e.g., "B110-1.3.26-B5.0" -> "1.3.26")
        def parts = PLATFORM_OS_VERSION_VALUE.split('-')
            def versionPart = parts[1]  // e.g., "1.3.26"
            def versionNumbers = versionPart.split('\\.')
            
            VERSION_MAJOR = versionNumbers[0]
            VERSION_MINOR = versionNumbers[1]
            VERSION_MICRO = versionNumbers[2]
            
            echo "${ANSI_BLUE}Extracted version - Major: ${VERSION_MAJOR}, Minor: ${VERSION_MINOR}, Micro: ${VERSION_MICRO}${ANSI_RESET}"
        } else {
            // PLATFORM_OS_VERSION provided but NOT in correct format
            echo "${ANSI_YELLOW}PLATFORM_OS_VERSION '${platformOsInput}' is not in required format (PREFIX-MJ.MN.MC-SUFFIX)${ANSI_RESET}"
            echo "${ANSI_BLUE}Checking OS_VERSION_VALUE parameter as fallback...${ANSI_RESET}"
            
            if (params.OS_VERSION_VALUE?.trim()) {
                def osVersionInput = params.OS_VERSION_VALUE.trim()
                
                if (osVersionInput ==~ validOsVersionPattern) {
                    // OS_VERSION_VALUE is valid (e.g., 1.3.26) - extract version numbers from it
                    def versionNumbers = osVersionInput.split('\\.')
                    VERSION_MAJOR = versionNumbers[0]
                    VERSION_MINOR = versionNumbers[1]
                    VERSION_MICRO = versionNumbers[2]
                    
                    // Keep the original PLATFORM_OS_VERSION as-is (even if not in standard format)
                    PLATFORM_OS_VERSION_VALUE = platformOsInput
                    echo "${ANSI_GREEN}Using provided PLATFORM_OS_VERSION: ${PLATFORM_OS_VERSION_VALUE}${ANSI_RESET}"
                    echo "${ANSI_BLUE}Extracted version from OS_VERSION_VALUE - Major: ${VERSION_MAJOR}, Minor: ${VERSION_MINOR}, Micro: ${VERSION_MICRO}${ANSI_RESET}"
                } else {
                    error("${ANSI_RED}❌ OS_VERSION_VALUE '${osVersionInput}' is not in required format (MJ.MN.MC). Exiting...${ANSI_RESET}")
                }
            } else {
                error("${ANSI_RED}❌ PLATFORM_OS_VERSION is invalid and OS_VERSION_VALUE is not provided. Please provide OS_VERSION_VALUE in MJ.MN.MC format. Exiting...${ANSI_RESET}")
            }
        }
    } else if (params.OS_VERSION_VALUE?.trim()) {
        // PLATFORM_OS_VERSION is empty, but OS_VERSION_VALUE is provided
        def osVersionInput = params.OS_VERSION_VALUE.trim()
        
        if (osVersionInput ==~ validOsVersionPattern) {
            def versionNumbers = osVersionInput.split('\\.')
            VERSION_MAJOR = versionNumbers[0]
            VERSION_MINOR = versionNumbers[1]
            VERSION_MICRO = versionNumbers[2]
            
            PLATFORM_OS_VERSION_VALUE = "${PROJECT_NAME}-${osVersionInput}-${BUILD_NUMBER}"
            echo "${ANSI_GREEN}Using OS_VERSION_VALUE to construct PLATFORM_OS_VERSION: ${PLATFORM_OS_VERSION_VALUE}${ANSI_RESET}"
            echo "${ANSI_BLUE}Extracted version - Major: ${VERSION_MAJOR}, Minor: ${VERSION_MINOR}, Micro: ${VERSION_MICRO}${ANSI_RESET}"
        } else {
            error("${ANSI_RED}❌ OS_VERSION_VALUE '${osVersionInput}' is not in required format (MJ.MN.MC). Exiting...${ANSI_RESET}")
        }
    } else {
        // Both PLATFORM_OS_VERSION and OS_VERSION_VALUE are empty - use date fallback
        PLATFORM_OS_VERSION_VALUE = "${PROJECT_NAME}-${formattedDate}-${BUILD_NUMBER}"
        echo "${ANSI_YELLOW}Both PLATFORM_OS_VERSION and OS_VERSION_VALUE are empty. Auto-generating: ${PLATFORM_OS_VERSION_VALUE}${ANSI_RESET}"
        
        // For auto-generated version, use date components (DD.MM.YY)
        def dateParts = formattedDate.split('\\.')
        VERSION_MAJOR = dateParts[0]  // DD
        VERSION_MINOR = dateParts[1]  // MM
        VERSION_MICRO = dateParts[2]  // YY
        
        echo "${ANSI_BLUE}Extracted version from date (DD.MM.YY) - Major: ${VERSION_MAJOR}, Minor: ${VERSION_MINOR}, Micro: ${VERSION_MICRO}${ANSI_RESET}"
    }
    
    // Export as environment variables for use in other stages and shell scripts
    env.PLATFORM_OS_VERSION_VALUE = PLATFORM_OS_VERSION_VALUE
    env.VERSION_MAJOR = VERSION_MAJOR
    env.VERSION_MINOR = VERSION_MINOR
    env.VERSION_MICRO = VERSION_MICRO
    env.VSV_VERSION = "${PROJECT_NAME}${VERSION_MAJOR}${VERSION_MINOR}${VERSION_MICRO}"
    env.OS_VERSION = "${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_MICRO}"
    
    // Update directory names with the platform OS version and store in env for cross-stage access
    SOFTWARE_RELEASE_BUILD_DIR = "${PROJECT_NAME}_${currentMonth}${currentDay}_#${BUILD_NUMBER}"
    AWS_SOFTWARE_RELEASE_BUILD_DIR = "${PROJECT_NAME}_${currentMonth}${currentDay}_#${BUILD_NUMBER}"
    AZURE_SOFTWARE_RELEASE_BUILD_DIR = "${PROJECT_NAME}_${currentMonth}${currentDay}_%23${BUILD_NUMBER}"
    
    // Store in environment for persistence across stages
    env.SOFTWARE_RELEASE_BUILD_DIR = SOFTWARE_RELEASE_BUILD_DIR
    env.AWS_SOFTWARE_RELEASE_BUILD_DIR = AWS_SOFTWARE_RELEASE_BUILD_DIR
    env.AZURE_SOFTWARE_RELEASE_BUILD_DIR = AZURE_SOFTWARE_RELEASE_BUILD_DIR
    
    echo "${ANSI_BLUE}SOFTWARE_RELEASE_BUILD_DIR: ${SOFTWARE_RELEASE_BUILD_DIR}${ANSI_RESET}"
    echo "${ANSI_BLUE}AZURE_SOFTWARE_RELEASE_BUILD_DIR: ${AZURE_SOFTWARE_RELEASE_BUILD_DIR}${ANSI_RESET}"
    
    // Update versions.json inside Docker container
    // Updates Level0.Product-Version, Level1.VSV, and Level2.OS-Version
    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        FILE=${IMX_YOCTO_DIR}/sources/${META_FRAMEWORK_DIR}/recipes-support/version/files/versions.json
        OTA_FILE=${IMX_YOCTO_DIR}/sources/${META_FRAMEWORK_DIR}/recipes-support/version/files/ota_version.json
        # Update version fields in versions.json to reflect the current build version.
        # This ensures downstream processes and release tracking systems have accurate version information
        # for traceability, artifact labeling, and automated reporting.
        if [ -f "\$FILE" ]; then
            echo "Updating versions in \$FILE"
            
            # Update Level0 -> Product-Version
            echo "Setting Product-Version to ${PLATFORM_OS_VERSION_VALUE}"
            sed -i 's/"Product-Version":[[:space:]]*"[^"]*"/"Product-Version": "${PLATFORM_OS_VERSION_VALUE}"/' "\$FILE"
            
            # Update Level1 -> VSV
            echo "Setting VSV to ${env.VSV_VERSION}"
            sed -i 's/"VSV":[[:space:]]*"[^"]*"/"VSV": "${env.VSV_VERSION}"/' "\$FILE"
            sed -i 's/"VSV":[[:space:]]*"[^"]*"/"VSV": "${env.VSV_VERSION}"/' "\$OTA_FILE"
            
            # Update Level2 -> OS-Version
            echo "Setting OS-Version to ${env.OS_VERSION}"
            sed -i 's/"OS-Version":[[:space:]]*"[^"]*"/"OS-Version": "${env.OS_VERSION}"/' "\$FILE"
            
            # Update Level3 -> SOC-Version
            echo "Setting SOC-Version to ${env.OS_VERSION}"
            sed -i 's/"SOC":[[:space:]]*"[^"]*"/"SOC": "${env.OS_VERSION}"/' "\$FILE"
            
            echo "✅ Updated all version fields"
            cat "\$FILE"
            cat "\$OTA_FILE"
        else
            echo "Missing file: \$FILE"
        fi
    """)
    
    echo "${ANSI_GREEN}✅ Platform OS version updated successfully${ANSI_RESET}"
}

/**
 * Updates the "CLOUD_ENV_INFO" field in build_env.conf inside the Docker container
 * to reflect the current build environment for traceability and automated reporting.
 *
 * No parameters; uses global environment variables.
 */
def updateCloudEnvInfo() {
    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        FILE=${IMX_YOCTO_DIR}/sources/${META_TELEMATICS_DIR}/recipes-vehicleinfo/ccmmanager/files/build_env.conf
        # Ensure CLOUD_ENV_INFO is propagated into build_env.conf for downstream usage.
        if [ -f "\$FILE" ]; then
            echo "Updating CLOUD_ENV_INFO in \$FILE to ${CLOUD_ENV_INFO}"
            if grep -q '"CLOUD_ENV_INFO"' "\$FILE"; then
                sed -i 's/"CLOUD_ENV_INFO"[[:space:]]*:[[:space:]]*"[^"]*"/"CLOUD_ENV_INFO" : "${CLOUD_ENV_INFO}"/' "\$FILE"
            else
                echo "CLOUD_ENV_INFO variable not found. So skipping update."
            fi
            echo "Resulting file:"
            cat "\$FILE"
        else
            echo "Missing file: \$FILE"
        fi
    """)
}

/**
 * Setup IMX mkimage repository
 */
def setupImxMkimage() {
    echo "${ANSI_BLUE}🔧 Setting up IMX mkimage...${ANSI_RESET}"
    
    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        echo "Setting up IMX mkimage repository..."
        
        # Clean and clone IMX mkimage
        rm -rf ${IMX_MKIMAGE_DIR}
        git clone ${IMX_MKIMAGE_REPO} ${IMX_MKIMAGE_DIR}
        cd ${IMX_MKIMAGE_DIR}
        
        # Handle LFS files and checkout branch
        git lfs pull 2>/dev/null || echo "No LFS files in IMX mkimage"
        git checkout ${IMX_MKIMAGE_TAG_OR_BRANCH}
        
        echo "✅ IMX mkimage setup complete"
    """)
    
    echo "${ANSI_GREEN}✅ IMX mkimage ready${ANSI_RESET}"
}

/**
 * Build WIC image with optimizations
 */
def buildWicImage() {
    echo "${ANSI_BLUE}🏗️ Building WIC image...${ANSI_RESET}"
    
    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        cd ${IMX_YOCTO_DIR}
        
        # Clean build if requested (mv + background rm for speed)
        if [ "${params.CLEAN_BUILD}" = "true" ]; then
            echo "🧹 Cleaning build directory..."
            rm -rf ${YOCTO_BUILD_DIR}_old 2>/dev/null || true
            if [ -d "${YOCTO_BUILD_DIR}" ]; then
                mv ${YOCTO_BUILD_DIR} ${YOCTO_BUILD_DIR}_old
                rm -rf ${YOCTO_BUILD_DIR}_old &
            fi
        fi
        
        echo "Setting up Yocto build environment..."
        export EULA=1
        DISTRO=fsl-imx-ABCSERIRES-wayland MACHINE=imx source imx-setup-release.sh -b ${YOCTO_BUILD_DIR}
        
        # Trim leading/trailing spaces coming from indented Jenkins choice parameters
        # Set HMI_VEHICLEBRAND and HMI_VARIANT without enclosing values in double quotes

        # Remove any previous definitions to avoid duplicates
        sed -i '/^HMI_VEHICLEBRAND[[:space:]]*=/d' conf/local.conf
        sed -i '/^HMI_VARIANT[[:space:]]*=/d' conf/local.conf

        # Append only if non-empty
        [ -n "${HMI_VEHICLEBRAND}" ] && echo "HMI_VEHICLEBRAND = \\\"${HMI_VEHICLEBRAND}\\\"" >> conf/local.conf
        [ -n "${HMI_VARIANT}" ] && echo "HMI_VARIANT = \\\"${HMI_VARIANT}\\\"" >> conf/local.conf

        # Remove any previous definitions for QT variables to avoid duplicates
        sed -i '/^QT_COMMERCIAL_GIT[[:space:]]*=/d' conf/local.conf
        sed -i '/^QT_COMMERCIAL_GIT_PROTOCOL[[:space:]]*=/d' conf/local.conf
        sed -i '/^QT_EDITION[[:space:]]*=/d' conf/local.conf
        sed -i '/^QT_COMMERCIAL_MODULES[[:space:]]*=/d' conf/local.conf

        # Append QT commercial settings (static values)
        echo 'QT_COMMERCIAL_GIT = "git://codereview.qt-project.org"' >> conf/local.conf
        echo 'QT_COMMERCIAL_GIT_PROTOCOL = "https"' >> conf/local.conf
        echo 'QT_EDITION = "commercial"' >> conf/local.conf
        echo 'QT_COMMERCIAL_MODULES = "1"' >> conf/local.conf

        # Limit BB_NUMBER_THREADS and PARALLEL_MAKE to prevent bitbake server starvation
        # BB_NUMBER_THREADS = concurrent bitbake tasks (cores - 4 to reserve for bitbake-server, hashserv, system)
        # PARALLEL_MAKE = make -j16 per task (trial run)
        ALLOWED_CORES=\$(nproc)
        BB_THREADS=\$((ALLOWED_CORES - 4))
        [ "\$BB_THREADS" -lt 1 ] && BB_THREADS=1
        MAKE_THREADS=16
        [ "\$MAKE_THREADS" -gt "\$ALLOWED_CORES" ] && MAKE_THREADS=\$ALLOWED_CORES
        sed -i '/^BB_NUMBER_THREADS/d' conf/local.conf
        sed -i '/^PARALLEL_MAKE/d' conf/local.conf
        echo "BB_NUMBER_THREADS = \\\"\${BB_THREADS}\\\"" >> conf/local.conf
        echo "PARALLEL_MAKE = \\\"-j \${MAKE_THREADS}\\\"" >> conf/local.conf
        echo "CPU budget: \${ALLOWED_CORES} cores -> BB_NUMBER_THREADS=\${BB_THREADS}, PARALLEL_MAKE=-j\${MAKE_THREADS} (max ~\$((BB_THREADS * MAKE_THREADS)) processes)"

        cat conf/local.conf
        
        echo "Adding required meta layers..."
        bitbake-layers add-layer ../sources/meta-ABCSERIRES
        bitbake-layers add-layer ../sources/meta-hmi
        bitbake-layers add-layer ../sources/meta-swupdate
        bitbake-layers add-layer ../sources/meta-framework
        bitbake-layers add-layer ../sources/meta-connectivity
        bitbake-layers add-layer ../sources/meta-configuration
        #bitbake-layers add-layer ../sources/meta-version-aggregator
        bitbake-layers add-layer ../sources/meta-image-optimization
        bitbake-layers add-layer ../sources/meta-secure-part
        bitbake-layers add-layer ../sources/meta-telematics
        bitbake-layers add-layer ../sources/wifiservice
        
        echo "Current layers configuration:"
        bitbake-layers show-layers

        # Kill any stale bitbake processes from previously aborted builds
        echo "Cleaning up stale bitbake processes..."
        pkill -9 -f bitbake || true
        sleep 2
        rm -f ${YOCTO_BUILD_DIR}/bitbake.lock ${YOCTO_BUILD_DIR}/bitbake.sock
        echo "Stale process cleanup done."

        rm -rf tmp/deploy/images/imx8qxpc0mek/rootfs.ext4
        rm -rf tmp/deploy/images/imx8qxpc0mek/boot.vfat
        rm -rf tmp/deploy/images/imx8qxpc0mek/flash_all_fastboot_mmcsda.ps1
        rm -rf tmp/deploy/images/imx8qxpc0mek/flash_all_fastboot_mmcsda.sh
        
        # Clear old ABCSERIRES* artifacts to prevent piling up during incremental builds
        echo "🧹 Clearing old ABCSERIRES* .wic, .swu, and .manifest files..."
        rm -f tmp/deploy/images/imx8qxpc0mek/ABCSERIRES*.wic || true
        rm -f tmp/deploy/images/imx8qxpc0mek/ABCSERIRES*.swu || true
        rm -f tmp/deploy/images/imx8qxpc0mek/ABCSERIRES*.manifest || true
        echo "✅ Old ABCSERIRES* artifacts cleared"
        
        echo "Starting WIC image build..."
        bitbake -c cleansstate googletest swupdate-image wpa-supplicant imx-image-full platformhmi versions gcc || true
        bitbake qtbase && bitbake qtdeclarative && bitbake imx-image-full

        # SDK Generation if requested
        if [ "${params.SDK_Generation}" = "true" ]; then
            echo "Starting SDK Generation build..."
            bitbake imx-image-full -c populate_sdk
            echo "✅ SDK Generation complete"
        fi
        
        echo "✅ WIC image build complete"
    """)
    
    echo "${ANSI_GREEN}✅ WIC image built successfully${ANSI_RESET}"
}

/**
 * Build OTA image
 */
def buildOtaImage() {
    echo "${ANSI_BLUE}🔄 Building OTA image...${ANSI_RESET}"
    
    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        cd ${IMX_YOCTO_DIR}
        
        echo "Setting up environment for OTA build..."
        source setup-environment ${YOCTO_BUILD_DIR}
        
        echo "Building SWUpdate image..."
        bitbake swupdate-image
        
        echo "✅ OTA image build complete"
    """)
    
    echo "${ANSI_GREEN}✅ OTA image built successfully${ANSI_RESET}"
    
    // Validate that required files are present inside the .swu package
    echo "${ANSI_BLUE}🔍 Validating SWU package contents...${ANSI_RESET}"
    def swuValidation = sh(
        script: """
            set -e
            SWU_DIR="${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/deploy/images/imx8qxpc0mek"
            SWU_FILE=\$(readlink -f "\${SWU_DIR}/swupdate-image-imx8qxpc0mek.rootfs.swu" 2>/dev/null || echo "")
            
            if [ -z "\${SWU_FILE}" ] || [ ! -f "\${SWU_FILE}" ]; then
                echo "ERROR: SWU file not found at \${SWU_DIR}/swupdate-image-imx8qxpc0mek.rootfs.swu"
                exit 1
            fi
            
            echo "Inspecting SWU file: \${SWU_FILE}"
            echo "--- SWU contents ---"
            cpio -itv < "\${SWU_FILE}"
            echo "--- End of SWU contents ---"
            
            REQUIRED_FILES="sw-description sw-description.sig imx-image-full-imx8qxpc0mek.rootfs.ext4.gz update.sh"
            MISSING_FILES=""
            SWU_CONTENTS=\$(cpio -it < "\${SWU_FILE}")
            
            for f in \${REQUIRED_FILES}; do
                if echo "\${SWU_CONTENTS}" | grep -q "^\${f}\$"; then
                    echo "✅ \${f} found in SWU package"
                else
                    echo "❌ \${f} is MISSING from SWU package!"
                    MISSING_FILES="\${MISSING_FILES} \${f}"
                fi
            done
            
            if [ -n "\${MISSING_FILES}" ]; then
                echo "ERROR: The following required files are missing from the SWU package:\${MISSING_FILES}"
                exit 1
            fi
        """,
        returnStatus: true
    )
    
    if (swuValidation != 0) {
        error("SWU package validation failed: One or more required files (sw-description, sw-description.sig, imx-image-full-imx8qxpc0mek.rootfs.ext4.gz, update.sh) are missing from the SWU package.")
    }
    
    echo "${ANSI_GREEN}✅ SWU package validation passed${ANSI_RESET}"
}

/**
 * Build bootloader
 */
def buildBootloader() {
    echo "${ANSI_BLUE}🥾 Building bootloader...${ANSI_RESET}"
    
    dockerExec("${YOCTO_PROJECT_CONTAINER_NAME}", """
        set -xe
        
        echo "Copying required bootloader files..."
        cp ${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/deploy/images/imx8qxpc0mek/imx-boot-tools/bl31-imx8qx.bin ${IMX_MKIMAGE_DIR}/iMX8QX/bl31.bin
        cp ${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/deploy/images/imx8qxpc0mek/imx-boot-tools/mx8qxc0-ahab-container.img ${IMX_MKIMAGE_DIR}/iMX8QX/
        cp ${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/work/imx8qxpc0mek-poky-linux/u-boot-imx/2024.04/deploy-u-boot-imx/u-boot-sd-2024.04-r0.bin ${IMX_MKIMAGE_DIR}/iMX8QX/u-boot.bin
        cp ${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/deploy/images/imx8qxpc0mek/imx-boot-tools/m4_image.bin ${IMX_MKIMAGE_DIR}/iMX8QX/
        cp ${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/deploy/images/imx8qxpc0mek/imx-boot-tools/u-boot-spl.bin-imx8qxpc0mek-sd ${IMX_MKIMAGE_DIR}/iMX8QX/u-boot-spl.bin
        
        echo "Building flash image..."
        cd ${IMX_MKIMAGE_DIR}
        make SOC=iMX8QX REV=c0 flash
        
        echo "✅ Bootloader generation complete"
    """)
    
    echo "${ANSI_GREEN}✅ Bootloader built successfully${ANSI_RESET}"
}

/**
 * Optimized artifact upload with retry mechanism
 */
def uploadArtifacts() {
    echo "${ANSI_BLUE}☁️ Uploading artifacts...${ANSI_RESET}"
    
    dir("${CONTAINER_BASE_WORKDIR}") {
        // Upload WIC and related files
        uploadWicArtifacts()
        
        // Upload flash.bin
        uploadFlashArtifacts()
    }
    
    echo "${ANSI_GREEN}✅ All artifacts uploaded successfully${ANSI_RESET}"
}

/**
 * Upload WIC-related artifacts
 */
def uploadWicArtifacts() {
    sh """
        set -xe
        cd ${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/deploy/images/imx8qxpc0mek
        
        echo "Preparing file paths..."
        
        # Function to rename file with platform_os_version-cluster-rootfs-timestamp pattern
        rename_with_pattern() {
            local src_link="\$1"
            local file_ext="\$2"
            local src_file=\$(readlink -f "\$src_link" 2>/dev/null)
            
            if [ -n "\$src_file" ] && [ -f "\$src_file" ]; then
                # Extract timestamp from original filename (e.g., imx-image-full-imx8qxpc0mek-20240315101530.rootfs.wic)
                local orig_basename=\$(basename "\$src_file")
                local timestamp=\$(echo "\$orig_basename" | grep -oE '[0-9]{14}' | head -1)
                
                # If no 14-digit timestamp found, try other patterns or use file modification time
                if [ -z "\$timestamp" ]; then
                    timestamp=\$(stat -c %Y "\$src_file" | xargs -I{} date -d @{} +%Y%m%d%H%M%S)
                fi
                
                local new_name="${PLATFORM_OS_VERSION_VALUE}-cluster-rootfs-\${timestamp}.\${file_ext}"
                local dest_dir=\$(dirname "\$src_file")
                local new_path="\${dest_dir}/\${new_name}"
                
                # Log to stderr so it doesn't get captured in variable assignment
                echo "Renaming \$src_file to \${new_path}" >&2
                mv "\$src_file" "\${new_path}"
                # Only output the new path to stdout (this gets captured)
                echo "\${new_path}"
            else
                echo ""
            fi
        }
        
        # Rename WIC file if exists
        if [ -L imx-image-full-imx8qxpc0mek.rootfs.wic ] || [ -f imx-image-full-imx8qxpc0mek.rootfs.wic ]; then
            WIC_FILE=\$(rename_with_pattern "imx-image-full-imx8qxpc0mek.rootfs.wic" "wic")
        else
            WIC_FILE=""
            echo "WIC file not found, skipping rename"
        fi
        
        # Rename SW_UPDATE file if exists
        if [ -L swupdate-image-imx8qxpc0mek.rootfs.swu ] || [ -f swupdate-image-imx8qxpc0mek.rootfs.swu ]; then
            SW_UPDATE_FILE=\$(rename_with_pattern "swupdate-image-imx8qxpc0mek.rootfs.swu" "swu")
        else
            SW_UPDATE_FILE=""
            echo "SWUpdate file not found, skipping rename"
        fi
        
        # Rename MANIFEST file if exists
        if [ -L imx-image-full-imx8qxpc0mek.rootfs.manifest ] || [ -f imx-image-full-imx8qxpc0mek.rootfs.manifest ]; then
            MANIFEST_FILE=\$(rename_with_pattern "imx-image-full-imx8qxpc0mek.rootfs.manifest" "manifest")
        else
            MANIFEST_FILE=""
            echo "Manifest file not found, skipping rename"
        fi
        
        EXT4_FILE=\$(readlink -f rootfs.ext4)
        vfat_FILE=\$(readlink -f boot.vfat)
        ps1=\$(readlink -f flash_all_fastboot_mmcsda.ps1)
        sh_script=\$(readlink -f flash_all_fastboot_mmcsda.sh)

        MIN_SIZE_MB=100
        MIN_SIZE_BYTES=\$((MIN_SIZE_MB*1024*1024))

        check_min_size() {
            local file="\$1"
            local label="\$2"
            if [ -z "\$file" ] || [ ! -f "\$file" ]; then
            echo "Size check skipped: \${label} not found: '\$file'"
            return 0
            fi
            local size
            size=\$(stat -c%s "\$file" 2>/dev/null || echo 0)
            local size_mb=\$(( size / 1024 / 1024 ))
            if [ "\$size" -lt "\$MIN_SIZE_BYTES" ]; then
            echo "ERROR: \${label} is too small: \${size_mb} MB (< \${MIN_SIZE_MB} MB). File: \$file"
            return 1
            fi
            echo "\${label} size OK: \${size_mb} MB (>= \${MIN_SIZE_MB} MB)."
            return 0
        }

        size_checks_failed=0
        if [ "${params.WIC_ImageGeneration}" = "true" ]; then
            check_min_size "\${WIC_FILE}" "WIC image" || size_checks_failed=1
            check_min_size "\${EXT4_FILE}" "EXT4 rootfs image" || size_checks_failed=1
            #check_min_size "\${vfat_FILE}" "VFAT boot image" || size_checks_failed=1
        fi
        if [ "${params.OTA_Generation}" = "true" ]; then
            check_min_size "\${SW_UPDATE_FILE}" "SWUpdate image" || size_checks_failed=1
        fi

        if [ "\$size_checks_failed" -ne 0 ]; then
            echo "One or more image files failed the minimum size check (\${MIN_SIZE_MB} MB). Aborting artifact upload."
            exit 1
        fi
        
        # Upload WIC images if generation was enabled
        if [ "${params.WIC_ImageGeneration}" = "true" ]; then
            if [ -f "\${WIC_FILE}" ]; then
                echo "📤 Uploading WIC image..."
                ${uploadWithRetry("\${WIC_FILE}")}
            else
                echo "ℹ️ WIC image not found, skipping upload"
            fi

            if [ -f "\${MANIFEST_FILE}" ]; then
                echo "📤 Uploading manifest..."
            ${uploadWithRetry("\${MANIFEST_FILE}")}
            else
                echo "ℹ️ Manifest not found, skipping upload"
            fi

            if [ -f "\${EXT4_FILE}" ]; then
                echo "📤 Uploading EXT4 image..."
                ${uploadWithRetry("\${EXT4_FILE}")}
            else
                echo "ℹ️ EXT4 image not found, skipping upload"
            fi

            if [ -f "\${vfat_FILE}" ]; then
                echo "📤 Uploading VFAT image..."
                ${uploadWithRetry("\${vfat_FILE}")}
            else
                echo "ℹ️ VFAT image not found, skipping upload"
            fi

            if [ -f "\${ps1}" ]; then
                echo "📤 Uploading flash_all_fastboot_mmcsda.ps1..."
                ${uploadWithRetry("\${ps1}")}
            else
                echo "ℹ️ flash_all_fastboot_mmcsda.ps1 not found, skipping upload"
            fi

            if [ -f "\${sh_script}" ]; then
                echo "📤 Uploading flash_all_fastboot_mmcsda.sh..."
                ${uploadWithRetry("\${sh_script}")}
            else
                echo "ℹ️ flash_all_fastboot_mmcsda.sh not found, skipping upload"
            fi
        fi
        
        # Upload OTA images if generation was enabled
        if [ "${params.OTA_Generation}" = "true" ]; then
            echo "📤 Uploading OTA image..."
            ${uploadWithRetry("\${SW_UPDATE_FILE}")}
        fi

        if [ "${params.SDK_Generation}" = "true" ]; then
            echo "📤 Uploading SDK image..."
            cd ../../sdk
            ls
            SDK_FILE=\$(readlink -f fsl-imx-ABCSERIRES-wayland-glibc-x86_64-imx-image-full-armv8a-imx8qxpc0mek-toolchain-6.6-nanbield.sh)
            echo "SDK_FILE path: \${SDK_FILE}"
            ${uploadWithRetry("\${SDK_FILE}")}
            
            # echo "📤 Uploading manifest..."
            # ${uploadWithRetry("\${MANIFEST_FILE}")}
        fi

        # Upload versions.json if present (use absolute path since cwd may have changed)
        VERSIONS_JSON_PATH="${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/work/imx8qxpc0mek-poky-linux/imx-image-full/1.0/rootfs/etc/config/versions.json"
        if [ -f "\${VERSIONS_JSON_PATH}" ]; then
            echo "📤 Uploading versions.json..."
            ${uploadWithRetry("\${VERSIONS_JSON_PATH}")}
        else
            echo "ℹ️ versions.json not found at \${VERSIONS_JSON_PATH}, skipping upload"
        fi

        OTHER_VERSION_JSON_PATH="${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/work/imx8qxpc0mek-poky-linux/imx-image-full/1.0/rootfs/etc/config/other_version.json"
        if [ -f "\${OTHER_VERSION_JSON_PATH}" ]; then
            echo "📤 Uploading other_version.json..."
            ${uploadWithRetry("\${OTHER_VERSION_JSON_PATH}")}
        else
            echo "ℹ️ other_version.json not found at \${OTHER_VERSION_JSON_PATH}, skipping upload"
        fi
        echo "✅ Artifact upload process completed"
        cat "\${VERSIONS_JSON_PATH}"
    """
}

/**
 * Upload flash artifacts
 */
def uploadFlashArtifacts() {
    sh """
        set -xe
        cd ${IMX_MKIMAGE_DIR}/iMX8QX
        echo "📤 Uploading flash.bin..."
        ${uploadWithRetry("flash.bin")}
    """
}

/**
 * Generate upload command with retry mechanism
 */
def uploadWithRetry(file, maxRetries = 2) {
    return """
        aws s3 cp "${file}" s3://abc-bucket/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/YOCTO_PROJECT/${PLATFORM}/${PROJECT_NAME}/${AWS_SOFTWARE_RELEASE_BUILD_DIR}/ 1>/dev/null
        for i in \$(seq 1 ${maxRetries}); do
            if sudo azcopy copy "${file}" 'https://${AZURE_STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/YOCTO_PROJECT/${PLATFORM}/${PROJECT_NAME}/${AZURE_SOFTWARE_RELEASE_BUILD_DIR}/?${SAS_KEY_INT}' --recursive; then
                echo "✅ Upload successful on attempt \$i"
                break
            elif [ \$i -eq ${maxRetries} ]; then
                echo "❌ Upload failed after ${maxRetries} attempts"
                # Create directory on Server B
                echo "Attempting fallback upload via SSH/SCP..."
                REMOTE_DIR="/home/swcocuser/BINARIES/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/YOCTO_PROJECT/${PLATFORM}/${PROJECT_NAME}/${SOFTWARE_RELEASE_BUILD_DIR}"
                ssh -o StrictHostKeyChecking=no swcocuser@xx.yy.zz.zz "mkdir -p \"\${REMOTE_DIR}\""
                if scp -o StrictHostKeyChecking=no "${file}" "swcocuser@xx.yy.zz.zz:\${REMOTE_DIR}/"; then
                    echo "✅ Fallback SCP upload successful"
                else
                    echo "❌ Fallback SCP upload failed"
                    exit 1
                fi
                #exit 1
            else
                echo "⚠️ Upload attempt \$i failed, retrying in 5 seconds..."
                sleep 2
            fi
        done
    """
}

/**
 * Extract project information with optimization
 */
def extractProjectInformation() {
    echo "${ANSI_BLUE}📊 Extracting project information...${ANSI_RESET}"
    
    // Map of project directory names to their environment variable prefix
    def projects = [
            'meta-ABCSERIRES': 'META_ABCSERIRES',
            'meta-hmi': 'META_HMI',
            'meta-framework': 'META_FRAMEWORK',
            'meta-connectivity': 'META_CONNECTIVITY',
            'meta-image-optimization': 'META_IMAGE_OPTIMIZATION',
            'meta-swupdate': 'META_SWUPDATE',
            'meta-version-aggregator': 'META_VERSION_AGGREGATOR',
            'meta-secure-part': 'META_SECURE_PART',
            'meta-configuration': 'META_CONFIGURATION',
            'meta-telematics': 'META_TELEMATICS',
            'wifiservice': 'WIFISERVICE'
        ]
        
    projects.each { projectDir, envPrefix ->
        def projectPath = "${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/sources/${projectDir}"
        
        try {
            // Check if directory exists
            def dirExists = sh(script: "[ -d '${projectPath}' ] && echo 'yes' || echo 'no'", returnStdout: true).trim()
            
            if (dirExists == 'yes') {
                // Get current branch (or detached HEAD info)
                def branchInfo = sh(script: """
                    cd '${projectPath}'
                    git symbolic-ref --short HEAD 2>/dev/null || git describe --tags --exact-match 2>/dev/null || git rev-parse --short HEAD
                        """, returnStdout: true).trim()
                        
                // Get current commit ID (short)
                def commitId = sh(script: "cd '${projectPath}' && git rev-parse --short HEAD", returnStdout: true).trim()
                
                // Get current commit ID (full)
                def commitIdFull = sh(script: "cd '${projectPath}' && git rev-parse HEAD", returnStdout: true).trim()
                
                // Store in environment variables
                env.setProperty("${envPrefix}_MANIFEST_BRANCH_OR_REVISION", branchInfo)
                env.setProperty("${envPrefix}_CURRENT_REVISION", commitId)
                env.setProperty("${envPrefix}_CURRENT_REVISION_FULL", commitIdFull)
                
                echo "${ANSI_GREEN}${projectDir}: Branch/Tag=${branchInfo}, Commit=${commitId}${ANSI_RESET}"
            } else {
                echo "${ANSI_YELLOW}⚠️ Project directory not found: ${projectPath}${ANSI_RESET}"
                env.setProperty("${envPrefix}_MANIFEST_BRANCH_OR_REVISION", '')
                env.setProperty("${envPrefix}_CURRENT_REVISION", '')
            }
            } catch (Exception e) {
                // Rethrow if it's an abort (FlowInterruptedException)
                if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
                     throw e
                }
            echo "${ANSI_YELLOW}⚠️ Warning: Could not get info for project ${projectDir}: ${e.message}${ANSI_RESET}"
            env.setProperty("${envPrefix}_MANIFEST_BRANCH_OR_REVISION", '')
            env.setProperty("${envPrefix}_CURRENT_REVISION", '')
            }
        }
        
    echo "${ANSI_GREEN}✅ Project information extracted successfully${ANSI_RESET}"
}

/**
 * Safely get an environment variable or return default value
 */
def safeGetVar(String varName, String defaultValue = 'N/A') {
    try {
        def value = env.getProperty(varName)
        return (value != null && value.trim() != '') ? value : defaultValue
    } catch (Exception e) {
        return defaultValue
    }
}

/**
 * Display comprehensive build summary
 */
def displayBuildSummary() {
    def metaLynxBranch = safeGetVar('META_ABCSERIRES_MANIFEST_BRANCH_OR_REVISION')
    def metaLynxCommit = safeGetVar('META_ABCSERIRES_CURRENT_REVISION')
    def metaHmiBranch = safeGetVar('META_HMI_MANIFEST_BRANCH_OR_REVISION')
    def metaHmiCommit = safeGetVar('META_HMI_CURRENT_REVISION')
    def buildDir = safeGetVar('SOFTWARE_RELEASE_BUILD_DIR')
    def containerName = safeGetVar('YOCTO_PROJECT_CONTAINER_NAME')
    
    echo """
${ANSI_BLUE}╔═══════════════════════════════════════════════════════════════════════════════╗
║                                BUILD SUMMARY                                 ║
╠═══════════════════════════════════════════════════════════════════════════════╣${ANSI_RESET}
${ANSI_GREEN}║ Build Number:        ${BUILD_NUMBER.padRight(50)}║
║ Build Directory:     ${buildDir.padRight(50)}║
║ Platform:            ${PLATFORM.padRight(50)}║
║ Project:            ${PROJECT_NAME.padRight(50)}║
║ PROJECT_NUMBER:      ${PROJECT_NUMBER.padRight(50)}║
║ Container:           ${containerName.padRight(50)}║${ANSI_RESET}
${ANSI_BLUE}╠═══════════════════════════════════════════════════════════════════════════════╣${ANSI_RESET}
${ANSI_PURPLE}║ META_ABCSERIRES Branch:    ${metaLynxBranch.padRight(50)}║
║ META_ABCSERIRES Commit:    ${metaLynxCommit.padRight(50)}║
║ META_HMI Branch:     ${metaHmiBranch.padRight(50)}║
║ META_HMI Commit:     ${metaHmiCommit.padRight(50)}║${ANSI_RESET}
${ANSI_BLUE}╚═══════════════════════════════════════════════════════════════════════════════╝${ANSI_RESET}
    """
}

/**
 * Enhanced success handler with comprehensive reporting
 */
def handleSuccess() {
    echo "${ANSI_GREEN}🎉 Pipeline succeeded! Preparing notifications...${ANSI_RESET}"
    
    script {
        try {
            
            // Generate change logs for all meta projects
            def changeLogResults = generateChangeLogsForAllProjects()

            // Fetch all versions (top-level string map from getVersions) AND re-parse nested keys
            def versionsFilePath = "${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/work/imx8qxpc0mek-poky-linux/imx-image-full/1.0/rootfs/etc/config/versions.json"

            def flatDisplayMap = [:]
            def htmlBuilder = new StringBuilder("<table border='1'><tr><th>Key</th><th>Value</th></tr>")

            if (fileExists(versionsFilePath)) {
                def rawJson = readFile versionsFilePath
                def parsed = new groovy.json.JsonSlurper().parseText(rawJson)

                echo "${ANSI_PURPLE}==== Parsed nested versions.json values ====${ANSI_RESET}"

                parsed.each { levelKey, levelVal ->
                    if (levelVal instanceof Map) {
                        levelVal.each { childKey, childVal ->
                            def k = "${childKey}"
                            def v = (childVal == null ? "" : childVal.toString())
                            echo "${ANSI_BLUE}${childKey} = ${v}${ANSI_RESET}"
                            flatDisplayMap[k] = v
                            htmlBuilder.append("<tr><td>${k}</td><td>${v}</td></tr>")
                            // Export as uppercase variable (OS_VERSION, BSP, FRAMEWORK, etc.)
                            def exportName = k.toUpperCase().replaceAll('[^A-Z0-9]', '_')
                            if (!['BUILD_NUMBER','PROJECT_NAME','PLATFORM'].contains(exportName)) {
                                env.setProperty(exportName, v)
                            }
                        }
                    } else {
                        // Non-map direct value
                        def v = (levelVal == null ? "" : levelVal.toString())
                        echo "${ANSI_BLUE}${levelKey} = ${v}${ANSI_RESET}"
                        flatDisplayMap[levelKey] = v
                        htmlBuilder.append("<tr><td>${levelKey}</td><td>${v}</td></tr>")
                        def exportName = levelKey.toUpperCase().replaceAll('[^A-Z0-9]', '_')
                        if (!['BUILD_NUMBER','PROJECT_NAME','PLATFORM'].contains(exportName)) {
                            env.setProperty(exportName, v)
                        }
                    }
                }
            } else {
                echo "${ANSI_YELLOW}versions.json not found at: ${versionsFilePath}${ANSI_RESET}"
            }

            htmlBuilder.append("</table>")
            env.VERSIONS_JSON_TABLE = htmlBuilder.toString()
            // Note: flatDisplayMap is a Map and cannot be stored in env; it's passed directly to prepareBuildDetails
            echo "${ANSI_GREEN}✅ VERSIONS_JSON_TABLE set${ANSI_RESET}"
            echo "${ANSI_GREEN}✅ VERSIONS_JSON_TABLE: ${env.VERSIONS_JSON_TABLE}${ANSI_RESET}"
            echo "${ANSI_PURPLE}VERSIONS_JSON_FLAT_MAP: ${flatDisplayMap}${ANSI_RESET}"

            // Prepare build details
            def buildDetails = prepareBuildDetails(flatDisplayMap)

            // Process ELM IDs and create links
            def elmProcessingResults = processElmIds(changeLogResults.elmIDs)
            
            // Generate email content
            def emailContent = generateEmailContent(buildDetails, changeLogResults.variables, elmProcessingResults)
            
            // Send email notification
            sendEmailNotification(emailContent)
            
            // Update Django database if enabled
            updateDjangoDatabase(buildDetails, changeLogResults.variables, elmProcessingResults.DATABASE_ELM_ID, flatDisplayMap)
            
            echo "${ANSI_GREEN}✅ Success handling completed${ANSI_RESET}"
            
        } catch (Exception e) {
            // Rethrow if it's an abort (FlowInterruptedException)
            if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
                throw e
            }
            echo "${ANSI_YELLOW}⚠️ Warning: Success handling encountered an error: ${e.message}${ANSI_RESET}"
        }
    }
}

/**
 * Prepare build details for reporting
 */
def prepareBuildDetails(flatDisplayMap) {
    return [
        todayDate: safeGetVar('TODAY_DATE', ''),
        yesterdayDate: safeGetVar('YESTERDAY_DATE', ''),
        date: safeGetVar('DATE', ''),
        buildNumber: BUILD_NUMBER,
        workspace: WORKSPACE,
        EMAIL_TO: EMAIL_TO ?: '',
        BUILD_NUMBER: BUILD_NUMBER,
        BUILD_URL: BUILD_URL,
        JOB_BASE_NAME: JOB_BASE_NAME,
        JOB_NAME: JOB_NAME,
        PROJECT_NAME: PROJECT_NAME ?: '',
        PLATFORM: PLATFORM ?: '',
        CONTAINER_NAME: CONTAINER_NAME,
        SLAVE_NODE_IP: NODE_LABELS ?: '',
        AZURE_CONTAINER_DIR: AZURE_CONTAINER_DIR ?: '',
        CLOUD_ENV_INFO: CLOUD_ENV_INFO ?: '',
        ABCSERIRES_MANIFEST_BRANCH_OR_TAG: ABCSERIRES_MANIFEST_BRANCH_OR_TAG ?: '',
        ABCSERIRES_MANIFEST_FILE: ABCSERIRES_MANIFEST_FILE ?: '',
        META_ABCSERIRES_BRANCH: safeGetVar('META_ABCSERIRES_MANIFEST_BRANCH_OR_REVISION', ''),
        META_ABCSERIRES_COMMIT_ID: safeGetVar('META_ABCSERIRES_CURRENT_REVISION', ''),
        META_ABCSERIRES_VERSION: flatDisplayMap['BSP'] ?: '',
        META_HMI_BRANCH: safeGetVar('META_HMI_MANIFEST_BRANCH_OR_REVISION', ''),
        META_HMI_COMMIT_ID: safeGetVar('META_HMI_CURRENT_REVISION', ''),
        META_HMI_VERSION: flatDisplayMap['HMI'] ?: '',
        META_FRAMEWORK_BRANCH: safeGetVar('META_FRAMEWORK_MANIFEST_BRANCH_OR_REVISION', ''),
        META_FRAMEWORK_COMMIT_ID: safeGetVar('META_FRAMEWORK_CURRENT_REVISION', ''),
        META_FRAMEWORK_VERSION: flatDisplayMap['Framework'] ?: '',
        META_CONNECTIVITY_BRANCH: safeGetVar('META_CONNECTIVITY_MANIFEST_BRANCH_OR_REVISION', ''),
        META_CONNECTIVITY_COMMIT_ID: safeGetVar('META_CONNECTIVITY_CURRENT_REVISION', ''),
        META_CONNECTIVITY_VERSION: flatDisplayMap['BT'] ?: '',
        META_VERSION_AGGREGATOR_BRANCH: safeGetVar('META_VERSION_AGGREGATOR_MANIFEST_BRANCH_OR_REVISION', ''),
        META_VERSION_AGGREGATOR_COMMIT_ID: safeGetVar('META_VERSION_AGGREGATOR_CURRENT_REVISION', ''),
        META_VERSION_AGGREGATOR_VERSION: flatDisplayMap['Product-Version'] ?: '',
        META_IMAGE_OPTIMIZATION_BRANCH: safeGetVar('META_IMAGE_OPTIMIZATION_MANIFEST_BRANCH_OR_REVISION', ''),
        META_IMAGE_OPTIMIZATION_COMMIT_ID: safeGetVar('META_IMAGE_OPTIMIZATION_CURRENT_REVISION', ''),
        META_IMAGE_OPTIMIZATION_VERSION: flatDisplayMap['IMAGE-OPTIMIZATION'] ?: '',
        META_SECURE_PART_BRANCH: safeGetVar('META_SECURE_PART_MANIFEST_BRANCH_OR_REVISION', ''),
        META_SECURE_PART_COMMIT_ID: safeGetVar('META_SECURE_PART_CURRENT_REVISION', ''),
        META_SECURE_PART_VERSION: flatDisplayMap['SECURE-PART'] ?: '',
        META_SWUPDATE_BRANCH: safeGetVar('META_SWUPDATE_MANIFEST_BRANCH_OR_REVISION', ''),
        META_SWUPDATE_COMMIT_ID: safeGetVar('META_SWUPDATE_CURRENT_REVISION', ''),
        META_SWUPDATE_VERSION: flatDisplayMap['SWUPDATE'] ?: '',
        META_CONFIGURATION_BRANCH: safeGetVar('META_CONFIGURATION_MANIFEST_BRANCH_OR_REVISION', ''),
        META_CONFIGURATION_COMMIT_ID: safeGetVar('META_CONFIGURATION_CURRENT_REVISION', ''),
        META_CONFIGURATION_VERSION: flatDisplayMap['CONFIGURATION'] ?: '',
        META_TELEMATICS_BRANCH: safeGetVar('META_TELEMATICS_MANIFEST_BRANCH_OR_REVISION', ''),
        META_TELEMATICS_COMMIT_ID: safeGetVar('META_TELEMATICS_CURRENT_REVISION', ''),
        META_TELEMATICS_VERSION: flatDisplayMap['Telematics'] ?: '',
        WIFISERVICE_BRANCH: safeGetVar('WIFISERVICE_MANIFEST_BRANCH_OR_REVISION', ''),
        WIFISERVICE_COMMIT_ID: safeGetVar('WIFISERVICE_CURRENT_REVISION', ''),
        WIFISERVICE_VERSION: flatDisplayMap['Wifi'] ?: '',
        BUILD_DISPLAY_NAME: safeGetVar('SOFTWARE_RELEASE_BUILD_DIR', '')
    ]
}

/**
 * Generate change logs for all projects
 */
def generateChangeLogsForAllProjects() {
    def projectChangeLogConfigs = [
        "META_ABCSERIRES": ["${IMX_YOCTO_DIR}/sources/${META_ABCSERIRES_DIR}", safeGetVar('META_ABCSERIRES_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_HMI": ["${IMX_YOCTO_DIR}/sources/${META_HMI_DIR}", safeGetVar('META_HMI_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_FRAMEWORK": ["${IMX_YOCTO_DIR}/sources/${META_FRAMEWORK_DIR}", safeGetVar('META_FRAMEWORK_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_CONNECTIVITY": ["${IMX_YOCTO_DIR}/sources/${META_CONNECTIVITY_DIR}", safeGetVar('META_CONNECTIVITY_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_CONFIGURATION": ["${IMX_YOCTO_DIR}/sources/${META_CONFIGURATION_DIR}", safeGetVar('META_CONFIGURATION_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_VERSION_AGGREGATOR": ["${IMX_YOCTO_DIR}/sources/${META_VERSION_AGGREGATOR_DIR}", safeGetVar('META_VERSION_AGGREGATOR_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_IMAGE_OPTIMIZATION": ["${IMX_YOCTO_DIR}/sources/${META_IMAGE_OPTIMIZATION_DIR}", safeGetVar('META_IMAGE_OPTIMIZATION_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_SWUPDATE": ["${IMX_YOCTO_DIR}/sources/${META_SWUPDATE_DIR}", safeGetVar('META_SWUPDATE_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_SECURE_PART": ["${IMX_YOCTO_DIR}/sources/${META_SECURE_PART_DIR}", safeGetVar('META_SECURE_PART_MANIFEST_BRANCH_OR_REVISION', '')],
        "META_TELEMATICS": ["${IMX_YOCTO_DIR}/sources/${META_TELEMATICS_DIR}", safeGetVar('META_TELEMATICS_MANIFEST_BRANCH_OR_REVISION', '')],
        "WIFISERVICE": ["${IMX_YOCTO_DIR}/sources/${WIFISERVICE_DIR}", safeGetVar('WIFISERVICE_MANIFEST_BRANCH_OR_REVISION', '')]
    ]
    
    def variables = [:]
    def elmIDs = []
    
    projectChangeLogConfigs.each { refTypePrefix, config ->
        try {
            def changeLogResult = changeLogForDailyBuilds(config[0], config[1], TODAY_DATE, YESTERDAY_DATE)
            variables["${refTypePrefix}_TAG_NAME"] = changeLogResult[0]
            variables["${refTypePrefix}_PREVIOUS_RELEASED_TAG_NAME"] = changeLogResult[1]
            variables["${refTypePrefix}_tagchanges_stat"] = changeLogResult[2]
            variables["${refTypePrefix}_COMMIT_COUNT"] = changeLogResult[3]
            //variables["${refTypePrefix}_VERSION"] = changeLogResult[5]
            elmIDs << changeLogResult[4]
        } catch (Exception e) {
            // Rethrow if it's an abort (FlowInterruptedException)
            if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
                throw e
            }
            echo "${ANSI_YELLOW}⚠️ Warning: Could not generate changelog for ${refTypePrefix}: ${e.message}${ANSI_RESET}"
        }
    }
    
    return [variables: variables, elmIDs: elmIDs]
}

/**
 * Process ELM IDs for reporting
 */
def processElmIds(elmIDs) {
    def flattenedElmIds = elmIDs.flatten().unique().sort()
    def DATABASE_ELM_ID = ""
    def ELM_ID_LINKS = "-"
    
    if (!flattenedElmIds.isEmpty()) {
        def base_url = "ELM_LINK_BASE_URL" // Replace with actual base URL for ELM links
        DATABASE_ELM_ID = flattenedElmIds.join(', ')
        ELM_ID_LINKS = flattenedElmIds.collect { id ->
            "<a href='${base_url}${id}' target='_blank'>${id}</a>"
        }.join(', ')
    }
    
    return [DATABASE_ELM_ID: DATABASE_ELM_ID, ELM_ID_LINKS: ELM_ID_LINKS]
}

/**
 * Generate email content
 */
def generateEmailContent(buildDetails, variables, elmProcessingResults) {
    buildDetails.ELM_ID = elmProcessingResults.ELM_ID_LINKS
    
    def htmlTemplate = readFile '/home/ubuntu/ABCSERIRES_YOCTO_PROJECT_success_email_template.html'
    htmlTemplate = replacePlaceholders(htmlTemplate, variables)
    htmlTemplate = replacePlaceholders(htmlTemplate, buildDetails)
    
    return htmlTemplate
}

/**
 * Send email notification
 */
def sendEmailNotification(htmlContent) {
    def dateVal = safeGetVar('DATE', '')
    dir("${WORKSPACE}") {
        writeFile file: "${PLATFORM}_${PROJECT_NAME}_${dateVal}_#${BUILD_NUMBER}.html", text: htmlContent
        sh """
            wkhtmltopdf "${WORKSPACE}/${PLATFORM}_${PROJECT_NAME}_${dateVal}_#${BUILD_NUMBER}.html" "${WORKSPACE}/${PLATFORM}_${PROJECT_NUMBER}_${dateVal}_#${BUILD_NUMBER}.pdf" || true
        """
    }
    
    emailext(
        subject: "[${PLATFORM}/${PROJECT_NAME}-PROD BUILD] --> #${BUILD_NUMBER}",
        body: htmlContent,
        to: "${EMAIL_TO}"
    )
}

/**
 * Update Django database if enabled
 */
def updateDjangoDatabase(buildDetails, changeLogVariables, databaseElmId, flatDisplayMap) {
    def djangoDatabaseUpdate = (DJANGO_DATABASE_UPDATE?.toBoolean() ?: false)
    
    if (djangoDatabaseUpdate) {
        try {
            echo "${ANSI_BLUE}📊 Starting Django database update...${ANSI_RESET}"
            
            // Safely get all variables before shell interpolation
            def metaLynxBranch = safeGetVar('META_ABCSERIRES_MANIFEST_BRANCH_OR_REVISION', '')
            def metaLynxCommit = safeGetVar('META_ABCSERIRES_CURRENT_REVISION', '')
            def metaHmiBranch = safeGetVar('META_HMI_MANIFEST_BRANCH_OR_REVISION', '')
            def metaHmiCommit = safeGetVar('META_HMI_CURRENT_REVISION', '')
            def metaFrameworkBranch = safeGetVar('META_FRAMEWORK_MANIFEST_BRANCH_OR_REVISION', '')
            def metaFrameworkCommit = safeGetVar('META_FRAMEWORK_CURRENT_REVISION', '')
            def metaConnectivityBranch = safeGetVar('META_CONNECTIVITY_MANIFEST_BRANCH_OR_REVISION', '')
            def metaConnectivityCommit = safeGetVar('META_CONNECTIVITY_CURRENT_REVISION', '')
            def metaSwupdateBranch = safeGetVar('META_SWUPDATE_MANIFEST_BRANCH_OR_REVISION', '')
            def metaSwupdateCommit = safeGetVar('META_SWUPDATE_CURRENT_REVISION', '')
            def metaImageOptBranch = safeGetVar('META_IMAGE_OPTIMIZATION_MANIFEST_BRANCH_OR_REVISION', '')
            def metaImageOptCommit = safeGetVar('META_IMAGE_OPTIMIZATION_CURRENT_REVISION', '')
            def metaVersionAggBranch = safeGetVar('META_VERSION_AGGREGATOR_MANIFEST_BRANCH_OR_REVISION', '')
            def metaVersionAggCommit = safeGetVar('META_VERSION_AGGREGATOR_CURRENT_REVISION', '')
            def metaSecurePartBranch = safeGetVar('META_SECURE_PART_MANIFEST_BRANCH_OR_REVISION', '')
            def metaSecurePartCommit = safeGetVar('META_SECURE_PART_CURRENT_REVISION', '')
            def dateVal = safeGetVar('DATE', '')
            
            sh """
                scp "${WORKSPACE}/${PLATFORM}_${PROJECT_NUMBER}_${dateVal}_#${BUILD_NUMBER}.pdf" ubuntu@10.64.0.31:/data/DASHBOARD_WEB/dashboard_Object_DB
                ssh ubuntu@10.64.0.31 'cd /home/ubuntu/RAG_Agent/Docker && docker compose run --rm django python manage.py ${database_create_instance} \
                    --project="${PROJECT_NUMBER}" \
                    --platform="${PLATFORM}" \
                    --ABCSERIRES_bsp_tag_name="${metaLynxBranch}" \
                    --ABCSERIRES_bsp_commit_id="${metaLynxCommit}" \
                    --ABCSERIRES_bsp_sw_version="${flatDisplayMap['BSP'] ?: ''}" \
                    --hmi_tag_name="${metaHmiBranch}" \
                    --hmi_commit_id="${metaHmiCommit}" \
                    --hmi_sw_version="${flatDisplayMap['HMI'] ?: ''}" \
                    --framework_tag_name="${metaFrameworkBranch}" \
                    --framework_commit_id="${metaFrameworkCommit}" \
                    --framework_sw_version="${flatDisplayMap['Framework'] ?: ''}" \
                    --connectivity_tag_name="${metaConnectivityBranch}" \
                    --connectivity_commit_id="${metaConnectivityCommit}" \
                    --connectivity_sw_version="${flatDisplayMap['BT'] ?: ''}" \
                    --swupdate_tag_name="${metaSwupdateBranch}" \
                    --swupdate_commit_id="${metaSwupdateCommit}" \
                    --image_optimization_tag_name="${metaImageOptBranch}" \
                    --image_optimization_commit_id="${metaImageOptCommit}" \
                    --version_aggregator_tag_name="${metaVersionAggBranch}" \
                    --version_aggregator_commit_id="${metaVersionAggCommit}" \
                    --version_aggregator_sw_version="${flatDisplayMap['Product-Version']}" \
                    --secure_part_tag_name="${metaSecurePartBranch}" \
                    --secure_part_commit_id="${metaSecurePartCommit}" \
                    --binary_path="${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${buildDetails.BUILD_DISPLAY_NAME}" \
                    --build_url="${BUILD_URL}" \
                    --build_id="${buildDetails.BUILD_DISPLAY_NAME}" \
                    --bugs="${databaseElmId}" \
                    --release_date="${dateVal}" \
                    --package_name="${flatDisplayMap['Product-Version']}" \
                    --email_attached_change_log="/data/DASHBOARD_WEB/dashboard_Object_DB/${PLATFORM}_${PROJECT_NUMBER}_${dateVal}_#${BUILD_NUMBER}.pdf" \
                    '
            """
            echo "${ANSI_GREEN}✅ Django database update completed successfully${ANSI_RESET}"
        } catch (Exception e) {
            // Rethrow if it's an abort (FlowInterruptedException)
            if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
                throw e
            }
            echo "${ANSI_RED}❌ Failed to update Django database: ${e.message}${ANSI_RESET}"
        }
    } else {
        echo "${ANSI_BLUE}ℹ️ Django database update is disabled${ANSI_RESET}"
    }
}

/**
 * Enhanced failure handler
 */
def handleFailure() {
    script {
        try {
            echo "${ANSI_RED}📧 Sending failure notification...${ANSI_RESET}"
            
            def htmlTemplate = readFile '/home/ubuntu/failure_email_template.html'
            def replacements = [
                BUILD_NUMBER: BUILD_NUMBER,
                BUILD_URL: BUILD_URL,
                FAILED_STAGE: FAILED_STAGE ?: 'Unknown',
                JOB_NAME: JOB_NAME,
                JOB_BASE_NAME: JOB_BASE_NAME
            ]
            
            replacements.each { key, value ->
                htmlTemplate = htmlTemplate.replaceAll(/\$\{${key}\}/, value)
            }
            
            emailext(
                subject: "[${PLATFORM}/${PROJECT_NAME}] Jenkins Pipeline Failed - ${JOB_NAME} #${BUILD_NUMBER}",
                body: htmlTemplate,
                to: "${EMAIL_TO}"
            )
            
            echo "${ANSI_GREEN}✅ Failure notification sent${ANSI_RESET}"
        } catch (Exception e) {
            // Rethrow if it's an abort (FlowInterruptedException)
            if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
                throw e
            }
            echo "${ANSI_RED}❌ Failed to send failure notification: ${e.message}${ANSI_RESET}"
        }
    }
}

// ========================================
// UTILITY FUNCTIONS (Optimized)
// ========================================

/**
 * Check and create directory with improved error handling
 */
def checkAndCreateDirectory(directoryPath) {
    if (sh(script: "[ -d '${directoryPath}' ]", returnStatus: true) != 0) {
        sh "mkdir -p '${directoryPath}'"
        echo "${ANSI_GREEN}📁 Created directory: ${directoryPath}${ANSI_RESET}"
    } else {
        echo "${ANSI_BLUE}📁 Directory exists: ${directoryPath}${ANSI_RESET}"
    }
}

/**
 * Optimized container management
 */
def startContainerIfNotRunning(containerName, imageName, workdir) {
    // Compute cpuset range: reserve top 2 cores for host OS/Jenkins, pin container to the rest
    def totalCores = sh(script: 'nproc', returnStdout: true).trim() as int
    def reservedCores = 2
    def cpusetMax = totalCores - reservedCores - 1
    def cpusetRange = "0-${cpusetMax}"
    echo "${ANSI_GREEN}🖥️ CPU Pinning: Total cores=${totalCores}, Reserved=${reservedCores} (cores ${cpusetMax + 1}-${totalCores - 1}), Container cpuset-cpus=${cpusetRange}${ANSI_RESET}"

    def containerStatus = sh(script: "docker inspect -f '{{.State.Running}}' ${containerName} 2>/dev/null || echo 'missing'", returnStdout: true).trim()
    
    switch(containerStatus) {
        case 'missing':
            echo "${ANSI_YELLOW}🐳 Creating new container: ${containerName}${ANSI_RESET}"
            sh """
                docker run -d -it --name ${containerName} -w ${workdir} \
                    --cpuset-cpus="${cpusetRange}" \
                    -v ${HOST_DIR}/${HOST_USER}/.ssh:${HOST_DIR}/${HOST_USER}/.ssh \
                    -v ${WORKSPACE}:${WORKSPACE} \
                    -v ${HOST_STORAGE_DIR}:${CONTAINER_BASE_WORKDIR} \
                    ${imageName} /bin/bash
            """
            break
        case 'false':
            echo "${ANSI_YELLOW}🔄 Starting existing container: ${containerName}${ANSI_RESET}"
            sh "docker start ${containerName}"
            sh "docker update --cpuset-cpus='${cpusetRange}' ${containerName}"
            break
        case 'true':
            echo "${ANSI_GREEN}✅ Container ${containerName} is already running${ANSI_RESET}"
            sh "docker update --cpuset-cpus='${cpusetRange}' ${containerName}"
            break
        default:
            echo "${ANSI_RED}❌ Unknown container status: ${containerStatus}${ANSI_RESET}"
    }
}

/**
 * Enhanced Docker execution with better error handling
 */
// def dockerExec(containerName, commands) {
//     sh "docker exec ${containerName} bash -c '''${commands}'''"
// }

/**
 * Enhanced Docker execution with safer quoting via heredoc to avoid shell-quoting issues
 */
def dockerExec(containerName, commands) {
    sh(script: """
        docker exec -i ${containerName} bash -s <<'__JENKINS_EOF__'
${commands}
__JENKINS_EOF__
    """)
}

/**
 * Parse git log output for commit analysis
 */
def formatGitLogOutput(logOutput) {
    def commits = []
    def currentCommit = [:]
    def inFileChanges = false
    def totalFilesChanged = 0
    def totalInsertions = 0
    def totalDeletions = 0
    def elmIDs = []

    logOutput.each { line ->
        if (line.startsWith("commit ")) {
            if (!currentCommit.isEmpty()) {
                currentCommit.totalFilesChanged = totalFilesChanged
                currentCommit.totalInsertions = totalInsertions
                currentCommit.totalDeletions = totalDeletions
                commits << currentCommit
                currentCommit = [:]
                inFileChanges = false
                totalFilesChanged = 0
                totalInsertions = 0
                totalDeletions = 0
            }
            currentCommit.commitHash = line.substring("commit ".length()).trim()
        } else if (line.startsWith("Author: ")) {
            currentCommit.author = line.substring("Author: ".length()).replaceAll(/\s*\(.*?\)/, "").trim()
        } else if (line.startsWith("Date: ")) {
            currentCommit.date = line.substring("Date: ".length()).trim()
        } else if (line.startsWith("    ")) {
            currentCommit.message = (currentCommit.message ?: "") + line.trim() + "<hr>"
            def numberMatches = (currentCommit.message ?: "").findAll(/\b\d{4,8}\b/)
            elmIDs.addAll(numberMatches)
        } else if (line.trim().isEmpty() && inFileChanges) {
            inFileChanges = false
        } else if (line.matches("\\d+\\s+\\d+\\s+.+")) {
            inFileChanges = true
            def parts = line.trim().split("\\s+")
            totalFilesChanged++
            totalInsertions += parts[0].toInteger()
            totalDeletions += parts[1].toInteger()
            currentCommit.fileChanges = (currentCommit.fileChanges ?: "") + parts[2] + "<br/>"
        }
    }

    if (!currentCommit.isEmpty()) {
        currentCommit.totalFilesChanged = totalFilesChanged
        currentCommit.totalInsertions = totalInsertions
        currentCommit.totalDeletions = totalDeletions
        commits << currentCommit
    }

    // Generate HTML table
    def htmlTable = """
        <table border="1">
        <tr>
        <th>Commit Hash</th>
        <th>Author</th>
        <th>Date</th>
        <th>Commit Message</th>
        <th>Total Files Changed</th>
        <th>Total Insertions</th>
        <th>Total Deletions</th>
        <th>File Changes</th>
        </tr>
    """
    
    commits.each { commit ->
        def message = commit.message ?: ""
        def fileChanges = commit.fileChanges ?: ""
        htmlTable += """
            <tr>
            <td>${commit.commitHash.take(10).trim()}</td>
            <td>${commit.author}</td>
            <td>${commit.date}</td>
            <td>${message.trim()}</td>
            <td>${commit.totalFilesChanged}</td>
            <td>${commit.totalInsertions}</td>
            <td>${commit.totalDeletions}</td>
            <td>${fileChanges.trim()}</td>
            </tr>
        """
    }

    if (commits.isEmpty()) {
        htmlTable += """ <tr><td colspan="8">No Changes</td></tr> """
    }

    htmlTable += "</table>"
    return [htmlTable, elmIDs]
}

/**
 * Generate change log for daily builds
 */
def changeLogForDailyBuilds(container, refName, todayDate, yesterdayDate) {
    script {
        dir("${HOST_STORAGE_DIR}/${container}") {
            sh """
                git config --global --add safe.directory "${HOST_STORAGE_DIR}/${container}"
            """

            def tagName = todayDate
            def previousTagName = "${yesterdayDate} 00:00"

            try {
                def logStatOutput = sh(script: "git log --since='${previousTagName}' --until='${tagName}' --numstat 2>/dev/null || echo ''", returnStdout: true).trim()
                def commitCount = sh(script: "git rev-list --count --since='${previousTagName}' --until='${tagName}' HEAD 2>/dev/null || echo '0'", returnStdout: true).trim() ?: "0"
                def version = sh(
                    script: """
                        set -xe
                        if [ -f appVersion.json ]; then
                            awk -F':' '{print \$2}' appVersion.json | tr -d '",'
                        elif [ -f recipes-common/config/files/config/versions.json ]; then
                            awk -F':' '{print \$2}' recipes-common/config/files/config/versions.json | tr -d '",' | head -n2
                        else
                            echo "UNKNOWN"
                        fi
                    """,
                    returnStdout: true
                ).trim()
                echo "VERSION: ${version}"

                def (tagchanges_stat, elmIDs) = formatGitLogOutput(logStatOutput.split("\\n"))

                return [tagName, previousTagName, tagchanges_stat, commitCount, elmIDs, version]
            } catch (Exception e) {
                // Rethrow if it's an abort (FlowInterruptedException)
                if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
                    throw e
                }
                echo "${ANSI_YELLOW}⚠️ Warning: Could not generate changelog for ${container}: ${e.message}${ANSI_RESET}"
                return [tagName, previousTagName, "No changes available", "0", []]
            }
        }
    }
}

/**
 * Replace placeholders in templates
 */
def replacePlaceholders(htmlTemplate, variables) {
    variables.each { key, value ->
        htmlTemplate = htmlTemplate.replaceAll(/\$\{${key}\}/, value ?: '')
    }
    return htmlTemplate
}

/**
 * Parse repo info output for project details
 */
def parseRepoInfoForProject(repoOutput) {
    def manifestBranch = ""
    def manifestMergeBranch = ""
    def projectDetails = [:]

    def lines = repoOutput.split("\n")
    
    lines.each { line ->
        if (line.startsWith("Manifest branch:")) {
            manifestBranch = line.split(":")[1].trim()
        } else if (line.startsWith("Manifest merge branch:")) {
            manifestMergeBranch = line.split(":")[1].trim()
        } else if (line.startsWith("Project:")) {   
            projectDetails.name = line.split(":")[1].trim()
        } else if (line.startsWith("Mount path:")) {
            projectDetails.mountPath = line.split(":")[1].trim()
        } else if (line.startsWith("Current revision:")) {
            projectDetails.currentRevision = line.split(":")[1].take(12).trim()
        } else if (line.startsWith("Current branch:")) {
            projectDetails.currentBranch = line.split(":")[1].take(12).trim()
        } else if (line.startsWith("Manifest revision:")) {
            projectDetails.manifestRevision = line.split(":")[1].trim()
        } else if (line.startsWith("Local Branches:")) {
            def branchesLine = line.replace("Local Branches:", "").trim()
            if (branchesLine == "0") {
                projectDetails.branchCount = 0
                projectDetails.localBranches = []
            } else {
                def countAndBranches = branchesLine =~ /(\d+)\s*\[(.+)\]/
                if (countAndBranches) {
                    projectDetails.branchCount = countAndBranches[0][1].toInteger()
                    projectDetails.localBranches = countAndBranches[0][2].split(", ").collect { it.trim() }
                }
            }
        }
    }

    return [
        manifestBranch      : manifestBranch,
        manifestMergeBranch : manifestMergeBranch,
        projectDetails      : projectDetails
    ]
}

/**
 * Parse a versions.json file and return a Map of key->value pairs.
 * Also echoes each parsed entry and (optionally) exports them as uppercase
 * environment-style Groovy variables for downstream usage.
 *
 * Usage example:
 *   def versions = parseVersionsJson("${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/work/imx8qxpc0mek-poky-linux/imx-image-full/1.0/rootfs/usr/bin/config/versions.json")
 *   echo "OS-Version parsed = ${versions['OS-Version']}"
 *
 * This function is safe: if file is missing or invalid JSON, it returns an empty map.
 */
def parseVersionsJson(filePath, exportVariables = true) {
    try {
        if (!filePath) {
            echo "${ANSI_YELLOW}parseVersionsJson: No file path provided${ANSI_RESET}"
            return [:]
        }
        if (!fileExists(filePath)) {
            echo "${ANSI_YELLOW}parseVersionsJson: File not found at ${filePath}${ANSI_RESET}"
            return [:]
        }

        def raw = readFile(filePath)
        def json = new groovy.json.JsonSlurper().parseText(raw)
        if (!(json instanceof Map)) {
            echo "${ANSI_YELLOW}parseVersionsJson: JSON root is not an object${ANSI_RESET}"
            return [:]
        }

        def result = [:]
        json.each { k, v ->
            def valueStr = v == null ? '' : v.toString()
            result[k.toString()] = valueStr
            echo "${ANSI_BLUE}versions.json: ${k} = ${valueStr}${ANSI_RESET}"
            if (exportVariables) {
                // Create a safe uppercase variable name (replace non-alnum with underscore)
                def varName = k.toString().toUpperCase().replaceAll('[^A-Z0-9]', '_')
                // Avoid clobbering existing critical vars inadvertently
                if (!['BUILD_NUMBER','PROJECT_NAME','PLATFORM'].contains(varName)) {
                    env.setProperty(varName, valueStr)
                }
            }
        }
        return result
    } catch (Exception e) {
        // Rethrow if it's an abort (FlowInterruptedException)
        if (e.getClass().getName().contains("FlowInterruptedException") || e.getClass().getName().contains("InterruptedException")) {
             throw e
        }
        echo "${ANSI_RED}parseVersionsJson: Failed to parse ${filePath}: ${e.message}${ANSI_RESET}"
        return [:]
    }
}

/**
 * Convenience wrapper that targets the standard framework versions.json location
 * and returns the parsed map. It uses parseVersionsJson internally.
 */
def getVersions(exportVariables = true) {
    def path = "${HOST_STORAGE_DIR}/${IMX_YOCTO_DIR}/${YOCTO_BUILD_DIR}/tmp/work/imx8qxpc0mek-poky-linux/imx-image-full/1.0/rootfs/etc/config/versions.json"
    return parseVersionsJson(path, exportVariables)
}

