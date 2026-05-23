/*
********** A14_DEV_aosp_jenkins_pipeine_script.groovy **********

Below are the list of build parameters to be created when triggering this AOSP build job.

1.AOSP_PROJECT_URL       ---> String Paramter  ---> Provide AOSP project URL(mandatory)
2.AOSP_TAG               ---> String Paramter  ---> Provide AOSP tag or branch or commit Id that you want to go for build.(mandatory)
3.CLUSTER_PROJECT_URL    ---> String Paramter  ---> Provide CLUSTER project URL(mandatory)
4.CLUSTER_TAG            ---> String Paramter  ---> Provide CLUSTER tag or branch or commit Id that you want to go for build.(mandatory)
5.ENVIRONMENT            ---> Choice Paramter  ---> Provide AOSP ENVIRONMENT Varibales like P360_STAGING, P360_PROD, UAT_QA(mandatory).
6.AOSP_PREVIOUS_RELEASED_TAG     ---> String Paramter  ---> Provide AOSP PREVIOUS released tag or branch or commit Id to fetch the change log.
7.CLUSTER_PREVIOUS_RELEASED_TAG  ---> String Paramter  ---> Provide CLUSTER PREVIOUS released tag or branch or commit Id to fetch the change log.
8.MAIN_VERSION           ---> String Paramter  ---> Provide AOSP MAIN VERSION that will be reading on the cluster. Ex: 1.48 ==> Here 1 is MAIN_VERSION and 48 is SUB VERSION(mandatory)
9.SUB_VERSION            ---> String Paramter  ---> Provide AOSP MAIN VERSION that will be reading on the cluster. Ex: 1.48 ==> Here 1 is MAIN_VERSION and 48 is SUB VERSION(mandatory)
10.Factory               ---> Boolean Paramter ---> Select/Enable the Factory if you are building the software for PRODUCTION. else uncheck/disable.(mandatory)
11.modified_Cluster_APK_version  ---> String Paramter  ---> Provide CLUSTER APK version that you want to overwrite the existing one.(mandatory)
12.EMAIL_TO              ---> String Paramter  ---> Provide EMAIL ID those who want to receive mail notifications on success and failures.(mandatory)
13.SLAVE_NODE_IP     ---> String Paramter  ---> Provide the IP address of the server or machine.

Note: The Difference between AOSP PROD and DAILY BUILDS/DEV scripts is that in the Fetching change logs.
*/


pipeline {
    agent {
        label "${params.SLAVE_NODE_IP}"
    }

    /**
     * Jenkins Pipeline Parameter Registration Notes:
     *
     * - Jenkins may display outdated parameter values, lagging behind by one build.
     * - Parameters are defined within the pipeline script, not in the job configuration.
     * - When a pipeline job is created from SCM (e.g., Git), Jenkins does not recognize parameters until the Jenkinsfile is parsed.
     * - The job configuration is updated with parameters only after the first successful pipeline run.
     */

    parameters {
        string(
            name: "ABC_MANIFEST_PROJECT_URL",
            defaultValue: "git@github.com:ABC-SW/abc-manifest.git",
            description: 'ABC_MANIFEST_PROJECT_URL (if not using manifest)'
        )
        string(
            name: "ABC_MANIFEST_BRANCH_OR_TAG",
            defaultValue: "master",
            description: 'ABC_MANIFEST_BRANCH_OR_TAG (if not using manifest), test-unpacking, master'
        )
        string(
            name: "MANIFEST_FILE",
            defaultValue: "a14_abc_manifest_dev.xml",
            description: 'Manifest file name for repo sync'
        )
        string(
            name: "RC_NAME",
            defaultValue: "",
            description: "Release Candidate Name (optional)"
        )
        booleanParam(
            name: 'CLEAN_REPO',
            defaultValue: false,
            description: 'Perform clean build (removes .repo and all other source directories)?'
        )
        booleanParam(
            name: 'CLEAN_BUILD',
            defaultValue: false,
            description: 'Perform clean build (removes output(out) directory)?'
        )
        booleanParam(
            name: 'CLONE',
            defaultValue: true,
            description: 'Enable repository cloning?'
        )
        booleanParam(
            name: 'AOSP_USER_BUILD',
            defaultValue: true,
            description: 'Build AOSP user variant?'
        )
        booleanParam(
            name: 'AOSP_DEBUG_BUILD',
            defaultValue: false,
            description: 'Build AOSP debug variant?'
        )
        booleanParam(
            name: 'GENERATE_OTA',
            defaultValue: false,
            description: 'Generate OTA packages?'
        )
        /*string(
            name: "MAIN_VERSION",
            defaultValue: "",
            description: 'Main version number (auto-generated if empty)'
        )
        string(
            name: "SUB_VERSION",
            defaultValue: "",
            description: 'Sub version number (auto-generated if empty)'
        )*/
        string(
            name: "BASE_AOSP_TAG_OR_BRANCH_NAME",
            defaultValue: "",
            description: 'default branch is QWM2290_Android14.0_R03_r002.5_2W ; Base AOSP(QWM2290_Android14.0_R03_r002.5_2W) tag/branch/commit ID'
        )
        string(
            name: "BTConnectionManager",
            defaultValue: "",
            description: 'BTConnectionManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "NavigationManager",
            defaultValue: "",
            description: 'NavigationManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "ProfileManager",
            defaultValue: "",
            description: 'ProfileManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "SystemHealthMonitor",
            defaultValue: "",
            description: 'SystemHealthMonitor tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "AudioServiceManager",
            defaultValue: "",
            description: 'AudioServiceManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "OtaManager",
            defaultValue: "",
            description: 'OtaManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "CodPapiManager",
            defaultValue: "",
            description: 'CodPapiManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "TwoWheelerService",
            defaultValue: "",
            description: 'TwoWheelerService tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "TelephonyManager",
            defaultValue: "",
            description: 'TelephonyManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "WifiManager",
            defaultValue: "",
            description: 'WifiManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "MediaServiceManager",
            defaultValue: "",
            description: 'MediaServiceManager tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "Projection",
            defaultValue: "",
            description: 'Projection tag/branch/commit ID(default branch is release/Integration-Branch_abc)'
        )
        string(
            name: "ThinkSeedRui",
            defaultValue: "",
            description: 'ThinkSeedRui tag/branch/commit ID(default branch is master)'
        )
        string(
            name: "ThinkWifi",
            defaultValue: "",
            description: 'ThinkWifi tag/branch/commit ID(default branch is master)'
        )
        string(
            name: "ABCHMIClientapk",
            defaultValue: "",
            description: 'ABCHMIClientapk tag/branch/commit ID(default branch is master)'
        )
        string(
            name: "ThinkCarPlay",
            defaultValue: "",
            description: 'ThinkCarPlay tag/branch/commit ID(default branch is master)'
        )
        string(
            name: "EMAIL_TO",
            defaultValue: "narendra.babu@abcmotor.com",
            description: 'Comma-separated list of email recipients for build notifications(sw-release@abcmotor.com)'
        )
        string(
            name: "SLAVE_NODE_IP",
            defaultValue: "buildserver_35_aws",
            description: 'Jenkins slave node IP address'
        )
        choice(
            name: "AZURE_CONTAINER_DIR",
            choices: ["dev", "internal"],
            description: 'Azure container directory for build artifacts'
        )
        string(
            name: "DOCKER_IMAGE_NAME",
            defaultValue: "abc_aosp12-jdk8_20.04_v2.0:latest",
            description: 'Docker image name for build containers'
        )
        string(
            name: "DOCKER_CONTAINER_NAME",
            defaultValue: "jenkins_cluster_app_a12_dev",
            description: 'Name of the main Jenkins build container'
        )
        booleanParam(
            name: "DJANGO_DATABASE_UPDATE",
            defaultValue: false,
            description: 'Update Django database after build'
        )
        booleanParam(
            name: "database_create_instance",
            defaultValue: false,
            description: 'Database create instance parameter'
        )
    }

    environment {
        // Set environment variables if needed
        ANSI_RESET = "\u001B[0m"
        ANSI_RED = "\u001B[31m"
        ANSI_BLUE = "\033[34m"
        ANSI_PURPLE = "\033[35m"
        ANSI_GREEN = "\033[32m"
        ANSI_YELLOW = "\033[33m"
        FAILED_STAGE = ""

        // OTA client and PKI project releated variables
        OTA_CLIENT_PROJECT_URL = "git@github.com:ABC-SW/4s45_aosp_vendor_ota_client.git"
        PKI_PROJECT_URL = "git@github.com:ABC-SW/4s45_pki_app.git"

        // Server/Host related varibales
        HOST_USER = "ubuntu"
        HOST_DIR = "/home"
        HOST_STORAGE_DIR = "${HOST_DIR}/${HOST_USER}/jenkins_workspace"

        // AOSP Software version related variables
        TYPE="A14"
        //AOSP_PREVIOUS_RELEASED_TAG = ""

        // Cluster releated variables
        ANDROID_SDK_ROOT = "/opt/Android/Sdk"
        CLUSTER_OUT_DIR_DEBUG = "apps/u388clusterapp/build/outputs/apk/debug"
        CLUSTER_OUT_DIR_RELEASE = "apps/u388clusterapp/build/outputs/apk/release"


        // Azure Storage releated items
        AZURE_STORAGE_ACCOUNT = "abcmazrndstauat01"
        CONTAINER_NAME = "integration"
        AZURE_BUILD_DIR = ""

        // Container Related items
        CONTAINER_BASE_WORKDIR = "/aosp_workspace"
        AOSP_UNPACKING_TOOL_PATH = "/data/tool/AOSP_A14"
        USER_UNPACKING_TOOL = "aosp-unpacking-tool_user"
        DEBUG_UNPACKING_TOOL = "aosp-unpacking-tool_debug"
        cluster_app_container = "jenkins_cluster_app_a14_abc_dev"
        aosp_user_container = "jenkins_user_workspace_a14_abc_dev"
        aosp_debug_container = "jenkins_debug_workspace_a14_abc_dev"

        // CACHE related variables - for AOSP 14
        USE_CCACHE = '1'
        CCACHE_EXEC = '/usr/bin/ccache'
        CCACHE_DIR_USER = "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/CCACHE/${aosp_user_container}/.ccache"
        CCACHE_DIR_DEBUG = "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/CCACHE/${aosp_debug_container}/.ccache"
        CCACHE_DIR_CLUSTER = "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/CCACHE/${cluster_app_container}/.ccache"
        
        // Build optimization variables for AOSP 14
        JACK_SERVER_VM_ARGUMENTS = "-Dfile.encoding=UTF-8 -XX:+TieredCompilation -Xmx4g"
        WITH_DEXPREOPT = 'true'
        SOONG_PARALLEL_JOBS = "16"
        NINJA_STATUS = "[%f/%t] "
        
        // Memory and performance optimizations
        ASAN_OPTIONS = "detect_leaks=0:halt_on_error=1:abort_on_error=1"
        MALLOC_CHECK_ = "0"

        // AOSP/UNPACKING TOOL DIR's
        ABC_TARGET_PRODUCT = "bengal_2w"
        ABC_TARGET_PRODUCT_QSSI = "qssi"
        ABC_TARGET_PRODUCT_DIR = "UM.9.15"
        ABC_TARGET_PRODUCT_QSSI_DIR = "QSSI.14"
        AOSP_OUT_DIR = "out/target/product/bengal_2w"
        UNPACKING_TOOL_OUT_DIR = "LA.UM.9.15.2/LINUX/android/out/target/product/bengal_2w"
        OUT_DIR = "output"
        PROJECT_NAME = "ABC"
        PROJECT_NUMBER = "ABC"

        // STORAGE_PATH RELATED TO THE SERVER
        STORAGE_PATH = ""
 
        // AZURE SAS TOKEN RELATED VARIBALES
        SAS_KEY_DEV = "xxx"
        SAS_KEY_INT = "yyy"

        BASE_AOSP_DIR = "QWM2290_Android14.0_R03_r002.5_2W"
        APK_BASE_DIR = "${BASE_AOSP_DIR}/QSSI.14/vendor/abc/packages/apps"
        BTConnectionManager_DIR = "${APK_BASE_DIR}/BTConnectionManager"
        NavigationManager_DIR = "${APK_BASE_DIR}/NavigationManager"
        ProfileManager_DIR = "${APK_BASE_DIR}/ProfileManager"
        SystemHealthMonitor_DIR = "${APK_BASE_DIR}/HealthMonitorManager"
        AudioServiceManager_DIR = "${APK_BASE_DIR}/AudioManager"
        OtaManager_DIR = "${APK_BASE_DIR}/OTAManager"
        TwoWheelerService_DIR = "${APK_BASE_DIR}/TwoWheelerService"
        TelephonyManager_DIR = "${APK_BASE_DIR}/PBAPService"
        WifiManager_DIR = "${APK_BASE_DIR}/WifiManager"
        MediaServiceManager_DIR = "${APK_BASE_DIR}/MediaManager"
        CODPAPIManager_DIR = "${APK_BASE_DIR}/CODPTelematics"
        Projection_DIR = "${APK_BASE_DIR}/Projection"
        ThinkSeedRui_DIR = "${APK_BASE_DIR}/ThinkSeedRui"
        ThinkWifi_DIR = "${APK_BASE_DIR}/ThinkWifi"
        ABCHMIClientapk_DIR = "${APK_BASE_DIR}/ABCHMIClient"
        ThinkCarPlay_DIR = "${APK_BASE_DIR}/ThinkCarPlay"

    }

    options {   
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()  // Avoid concurrent builds to prevent cache corruption
        timeout(time: 12, unit: 'HOURS')  // Extended timeout for AOSP 14 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout()  // Skip default checkout for better control
        retry(1)  // Retry once on failure for transient issues
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}=== ${STAGE_NAME} ===${ANSI_RESET}"
                        
                        // Clean workspace
                        cleanWs()
                        
                        // Get the current directory using the pwd() function
                        def currentDir = pwd()

                        // Getting DATE and TIME info with single call
                        def dateInfo = sh(script: """
                            NOW=\$(date +'%Y-%m-%d %H:%M')
                            TODAY=\$(date +'%Y-%m-%d')
                            YESTERDAY=\$(date -d "yesterday" +'%Y-%m-%d')
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
                        
                        // Assigning Values for MAIN_VERSION and SUB_VERSION based on Month and Day if those parameters are empty input variables.
                        /*if (params.MAIN_VERSION == "") {
                            MAIN_VERSION = "${currentMonth}"
                        } else {
                            MAIN_VERSION = params.MAIN_VERSION
                        }
                        if (params.SUB_VERSION == "") {
                            SUB_VERSION = "${currentDay}"
                        } else {
                            SUB_VERSION = params.SUB_VERSION
                        }*/

                        def package_name = params.RC_NAME ?: ''
                        if (package_name == "") {
                            RC_NAME = "${PROJECT_NUMBER}_${currentMonth}.${currentDay}"
                        } else {
                            RC_NAME = package_name
                        }

                        echo "${ANSI_RED}RC_NAME: ${RC_NAME}${ANSI_RESET}"
                        echo "${ANSI_PURPLE}TODAY DATE: ${TODAY_DATE}${ANSI_RESET}"
                        echo "${ANSI_PURPLE}YESTERDAY DATE: ${YESTERDAY_DATE}${ANSI_RESET}"

                        // Build directory names
                        SOFTWARE_RELEASE_BUILD_DIR = "${currentMonth}${currentDay}/${RC_NAME}_#${BUILD_NUMBER}"
                        AZURE_SOFTWARE_RELEASE_BUILD_DIR = "${currentMonth}${currentDay}/${RC_NAME}_%23${BUILD_NUMBER}"
                        AWS_SOFTWARE_RELEASE_BUILD_DIR = "${currentMonth}${currentDay}/${RC_NAME}_#${BUILD_NUMBER}"

                        // Initialize infrastructure
                        initializeInfrastructure()
                        
                        // Get system information
                        numOfCores = sh(returnStdout: true, script: 'nproc').trim()
                        echo "${ANSI_GREEN}Available CPU cores: ${numOfCores}${ANSI_RESET}"

                        // Defining build variables which are not defined by On JENKINS UI to avoid "VARIABLE NOT FOUND" ERRORS.
                        def aospPreviousReleasedTag = params.AOSP_PREVIOUS_RELEASED_TAG ?: ''
                        def email_to = params.EMAIL_TO ?: ''

                        if (aospPreviousReleasedTag == "") {
                            AOSP_PREVIOUS_RELEASED_TAG = ""
                        } else {
                            AOSP_PREVIOUS_RELEASED_TAG = aospPreviousReleasedTag
                        }
                        if (email_to == "") {
                            EMAIL_TO = "narendra.babu@abcmotor.com"
                        } else {
                            EMAIL_TO = email_to
                        }

                        echo "${ANSI_RED}ABC_TARGET_PRODUCT_DIR: ${ABC_TARGET_PRODUCT_DIR}${ANSI_RESET}"
                        echo "${ANSI_RED}ABC_TARGET_PRODUCT: ${ABC_TARGET_PRODUCT}${ANSI_RESET}"
                        echo "${ANSI_PURPLE} AOSP_PREVIOUS_RELEASED_TAG: ${AOSP_PREVIOUS_RELEASED_TAG}${ANSI_RESET}"
                        echo "${ANSI_PURPLE} EMAIL_TO: ${EMAIL_TO}${ANSI_RESET}"
                        
                        // Display build summary
                        displayBuildSummary()
                        
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
                        
                        // Determine which containers to process based on build variant selections
                        def targetContainers = []
                        if (params.AOSP_USER_BUILD) {
                            echo "${ANSI_GREEN}Including USER container: ${aosp_user_container}${ANSI_RESET}"
                            targetContainers << aosp_user_container
                        }
                        if (params.AOSP_DEBUG_BUILD) {
                            echo "${ANSI_GREEN}Including USERDEBUG container: ${aosp_debug_container}${ANSI_RESET}"
                            targetContainers << aosp_debug_container
                        }

                        if (targetContainers.isEmpty()) {
                            echo "${ANSI_YELLOW}No build variants selected (AOSP_USER_BUILD / AOSP_DEBUG_BUILD both false). Skipping AOSP repo initialization.${ANSI_RESET}"
                        } else {
                            // Initialize repositories only for required containers
                            targetContainers.each { c ->
                                initializeAospRepository(c)
                            }

                            // Checkout branches in each selected container (kept separate in case of future divergence)
                            targetContainers.each { c ->
                                checkoutAllBranches(c)
                            }

                            // Extract project info from primary container (prefer user, else debug)
                            def primaryContainer = params.AOSP_USER_BUILD ? aosp_user_container : aosp_debug_container
                            extractProjectInformation(primaryContainer)
                        }

                        // Display updated build summary with commit info
                        displayBuildSummary()
                        
                    } catch (Exception e) {
                        handleStageFailure(STAGE_NAME, e)
                    }
                }
            }
        }

        stage('AOSP_USER_BUILD') {
            when {
                expression { params.AOSP_USER_BUILD }
            }
            steps {
                script {
                    aospBuild([
                        stageName: 'AOSP_USER_BUILD',
                        container: aosp_user_container,
                        sourceDir: "${BASE_AOSP_DIR}",
                        buildVariant: "user",
                        unpackingTool: USER_UNPACKING_TOOL
                    ])
                }
            }
        }

        stage('AOSP_DEBUG_BUILD') {
            when {
                expression { params.AOSP_DEBUG_BUILD }
            }
            steps {
                script {
                    aospBuild([
                        stageName: 'AOSP_DEBUG_BUILD',
                        container: aosp_debug_container,
                        sourceDir: "${BASE_AOSP_DIR}",
                        buildVariant: "userdebug",
                        unpackingTool: DEBUG_UNPACKING_TOOL
                    ])
                }
            }
        }
    }

    post {
        always {
            // Clean up resources or perform actions that need to be done in all cases
            echo "[Always] Pipeline succeeded! Sending notifications..."
        }
        success {
            handleSuccess()
        }

        failure {
            handleFailure()
        }
    }
}

/**
 * Handle build failure
 */
def handleFailure() {
    echo "${ANSI_RED}💥 Build failed!${ANSI_RESET}"
    
    script {
        def htmlTemplate = readFile '/data/tool/AOSP_A12/failure_email_template.html'
        
        // Replace placeholders with actual Jenkins variables
        htmlTemplate = htmlTemplate.replaceAll(/\$\{BUILD_NUMBER\}/, "${BUILD_NUMBER}")
        htmlTemplate = htmlTemplate.replaceAll(/\$\{BUILD_URL\}/, "${BUILD_URL}")
        htmlTemplate = htmlTemplate.replaceAll(/\$\{FAILED_STAGE\}/, "${FAILED_STAGE}")
        htmlTemplate = htmlTemplate.replaceAll(/\$\{JOB_NAME\}/, "${JOB_NAME}")
        htmlTemplate = htmlTemplate.replaceAll(/\$\{JOB_BASE_NAME\}/, "${JOB_BASE_NAME}")
        htmlTemplate = htmlTemplate.replaceAll(/\$\{PROJECT_NAME\}/, "${PROJECT_NAME}")

        emailext \
            subject: """[${PROJECT_NAME}-BUILD FAILED] ❌ Jenkins Pipeline - Build #${BUILD_NUMBER} Failed at ${FAILED_STAGE}""",
            body: htmlTemplate,
            to: "${params.EMAIL_TO}",
            mimeType: 'text/html'
    }
}

/**
 * Generate comprehensive build summary
 */

// ========================================
// OPTIMIZED CORE FUNCTIONS
// ========================================

/**
 * Centralized error handling for all stages
 */
def handleStageFailure(stageName, exception) {
    echo "${ANSI_RED}❌ FAILED: ${stageName} stage failed: ${exception.message}${ANSI_RESET}"
    currentBuild.result = 'FAILURE'
    FAILED_STAGE = stageName
    throw exception
}

/**
 * Initialize infrastructure components for AOSP build
 */
def initializeInfrastructure() {
    echo "${ANSI_BLUE}🔧 Initializing infrastructure...${ANSI_RESET}"
    
    // Create required directories
    def directories = [
        "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${aosp_user_container}",
        "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${aosp_debug_container}",
        "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${cluster_app_container}",
        "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/CCACHE/${aosp_user_container}/.ccache",
        "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/CCACHE/${aosp_debug_container}/.ccache",
        "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/CCACHE/${cluster_app_container}/.ccache"
    ]
    
    directories.each { dir ->
        checkAndCreateDirectory(dir)
    }
    
    // Start containers with check
    echo "${ANSI_BLUE}Starting AOSP containers...${ANSI_RESET}"
    startContainerIfNotRunning("${aosp_user_container}", "${params.DOCKER_IMAGE_NAME}", "${CONTAINER_BASE_WORKDIR}/${aosp_user_container}", "${AOSP_UNPACKING_TOOL_PATH}", "${USER_UNPACKING_TOOL}", "${CCACHE_DIR_USER}")
    startContainerIfNotRunning("${aosp_debug_container}", "${params.DOCKER_IMAGE_NAME}", "${CONTAINER_BASE_WORKDIR}/${aosp_debug_container}", "${AOSP_UNPACKING_TOOL_PATH}", "${DEBUG_UNPACKING_TOOL}", "${CCACHE_DIR_DEBUG}")
    
    // Initialize global variables
    // aospCommitID_DEBUG = ""
    // aospCommitID_USER = ""
    
    // Setup CCACHE optimizations for AOSP 14
    //setupCcacheOptimizations()
    
    echo "${ANSI_GREEN}✅ Infrastructure initialized successfully${ANSI_RESET}"
}

/**
 * Setup container environment (avoid repetition)
 */
def setupContainerEnvironment() {
    echo "${ANSI_BLUE}🐳 Setting up container environment...${ANSI_RESET}"
    
    // Installing the required packages on the containers with cluster container
    // installingPackagesOnDocker("${aosp_user_container}")
    // installingPackagesOnDocker("${aosp_debug_container}")
    
    echo "${ANSI_GREEN}✅ Container environment ready${ANSI_RESET}"
}

/**
 * Setup CCACHE optimizations specifically for AOSP 14
 */
def setupCcacheOptimizations() {
    echo "${ANSI_BLUE}⚡ Setting up CCACHE optimizations for AOSP 14...${ANSI_RESET}"
    
    [aosp_user_container, aosp_debug_container].each { container ->
        sh """
        docker exec ${container} bash -c '
            export CCACHE_EXEC=/usr/bin/ccache &&
            export USE_CCACHE=1 &&
            ccache -M 150G &&
            ccache -o compression=true &&
            ccache -o compression_level=6 &&
            ccache -o max_files=0 &&
            ccache -o sloppiness=file_macro,locale,time_macros &&
            ccache -s &&
            echo "CCACHE optimized for AOSP 14 build performance"
        '
        """
    }
    
    echo "${ANSI_GREEN}✅ CCACHE optimizations configured${ANSI_RESET}"
}

/**
 * Initialize AOSP repository using repo manifest
 */
def initializeAospRepository(container) {
    echo "${ANSI_BLUE}📦 Initializing AOSP repository...${ANSI_RESET}"
    
    // Determine which manifest to use based on parameters or environment
    
    echo "${ANSI_GREEN}Manifest URL: ${params.ABC_MANIFEST_PROJECT_URL}${ANSI_RESET}"
    echo "${ANSI_GREEN}Manifest Branch: ${params.ABC_MANIFEST_BRANCH_OR_TAG}${ANSI_RESET}"
    echo "${ANSI_GREEN}Manifest File: ${params.MANIFEST_FILE}${ANSI_RESET}"
    
    // Clone AOSP for both user and debug containers using repo manifest
    cloneOrUpdateAospWithManifest("${container}", "${params.ABC_MANIFEST_PROJECT_URL}", "${CONTAINER_BASE_WORKDIR}/${container}", "${params.ABC_MANIFEST_BRANCH_OR_TAG}", "${params.MANIFEST_FILE}")
    //cloneOrUpdateAospWithManifest("${aosp_debug_container}", "${params.ABC_MANIFEST_PROJECT_URL}", "${CONTAINER_BASE_WORKDIR}/${aosp_debug_container}", "${params.ABC_MANIFEST_BRANCH_OR_TAG}", "${params.MANIFEST_FILE}")
    
    echo "${ANSI_GREEN}✅ AOSP repository initialized successfully${ANSI_RESET}"
}

/**
 * Extract project information with optimization
 */

def extractProjectInformation(container) {
    echo "${ANSI_BLUE}📊 Extracting project information...${ANSI_RESET}"
    
    dir("${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${container}") {
        def projects = [
            "QWM2290_Android14.0_R03_r002.5_2W",
            "btservicemanagerapk",
            "navigationmanagerapk",
            "profilemanagerapk",
            "systemhealthmonitorapk",
            "abcaudioservicemanagerapk",
            "otamanagerapk",
            "codpapimanagerapk",
            "wifimanagerapk",
            "mediaservicemanagerapk",
            "twowheelerserviceapk",
            "telephonymanagerapk",
            "aaprojectionapk",
            "ThinkWifi",
            "ThinkSeedRui",
            'ABCHMIClientapk',
            'ThinkCarPlay'

        ]
        
        def projectInfoMap = [:]
        
        projects.each { projectName ->
            try {
                def repoInfoOutput = sh(script: "repo info ${projectName} 2>/dev/null || echo 'No info available'", returnStdout: true).trim()
                
                if (repoInfoOutput != 'No info available') {
                    def parsedRepoInfo = parseRepoInfoForProject(repoInfoOutput)
                    def safeProjectName = projectName.toUpperCase().replaceAll('[-\\.]', '_')
                    
                    // Store project information
                    projectInfoMap["${safeProjectName}_CURRENT_BRANCH"] = parsedRepoInfo.manifestBranch
                    projectInfoMap["${safeProjectName}_MANIFEST_BRANCH"] = parsedRepoInfo.manifestMergeBranch
                    projectInfoMap["${safeProjectName}_MOUNT_PATH"] = parsedRepoInfo.projectDetails.mountPath
                    projectInfoMap["${safeProjectName}_NAME"] = parsedRepoInfo.projectDetails.name
                    projectInfoMap["${safeProjectName}_CURRENT_REVISION"] = parsedRepoInfo.projectDetails.currentRevision
                    projectInfoMap["${safeProjectName}_MANIFEST_BRANCH_OR_REVISION"] = parsedRepoInfo.projectDetails.manifestRevision
                    
                    // Get current branch and commit details
                    if (parsedRepoInfo.projectDetails.currentBranch) {
                        projectInfoMap["${safeProjectName}_MANIFEST_BRANCH_OR_REVISION"] = parsedRepoInfo.projectDetails.currentBranch
                        def commitID = sh(script: "cd ${parsedRepoInfo.projectDetails.mountPath} && git rev-parse --short HEAD 2>/dev/null || echo 'unknown'", returnStdout: true).trim()
                        projectInfoMap["${safeProjectName}_CURRENT_REVISION"] = commitID
                    }
                    
                    echo "${ANSI_GREEN}${projectName}: Branch=${parsedRepoInfo.projectDetails.manifestRevision}, Revision=${projectInfoMap["${safeProjectName}_CURRENT_REVISION"]}${ANSI_RESET}"
                }
            } catch (Exception e) {
                echo "${ANSI_YELLOW}⚠️ Warning: Could not get info for project ${projectName}: ${e.message}${ANSI_RESET}"
            }
        }
        
        // Set global variables for use in other stages
        projectInfoMap.each { key, value ->
            binding.setVariable(key, value)
        }
    }
    
    echo "${ANSI_GREEN}✅ Project information extracted successfully${ANSI_RESET}"
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
 * Display comprehensive build summary
 */
def displayBuildSummary() {
    echo """
${ANSI_BLUE}╔═══════════════════════════════════════════════════════════════════════════════╗
║                              AOSP BUILD SUMMARY                              ║
╠═══════════════════════════════════════════════════════════════════════════════╣${ANSI_RESET}
${ANSI_GREEN}║ Build Number:        ${BUILD_NUMBER.padRight(50)}║
║ Build Directory:     ${(SOFTWARE_RELEASE_BUILD_DIR ?: 'N/A').padRight(50)}║
║ Platform:            ${PROJECT_NUMBER.padRight(50)}║
║ AOSP Type:           ${TYPE.padRight(50)}║
║ Version:             ${RC_NAME}.padRight(47)}║${ANSI_RESET}
${ANSI_BLUE}╠═══════════════════════════════════════════════════════════════════════════════╣${ANSI_RESET}
${ANSI_PURPLE}║ USER Container:      ${(aosp_user_container ?: 'N/A').padRight(50)}║
║ DEBUG Container:     ${(aosp_debug_container ?: 'N/A').padRight(50)}║
${ANSI_BLUE}╚═══════════════════════════════════════════════════════════════════════════════╝${ANSI_RESET}
    """
}

// checking And Creating Directory if doesn't exist on the server 
def checkAndCreateDirectory(directoryPath) {
    script {
        // Check if the directory exists
        def dirExists = sh(script: "[ -d '${directoryPath}' ]", returnStatus: true) == 0
        
        // If directory doesn't exist, create it
        if (!dirExists) {
            sh """mkdir -p ${directoryPath}"""
            echo "${ANSI_RED}Directory created: ${directoryPath} ${ANSI_RESET}"
        } else {
            echo "${ANSI_RED}Directory already exists: ${directoryPath} ${ANSI_RESET}"
        }
    }
}

// Creates and starts containers(CLUSTER, USER, DEBUG containers) if not created/started already - 
def startContainerIfNotRunning(containerName, imageName, workdir, base_unpacking_tool_path, variant_path, CCACHE_DIR) {
    def containerStatus = sh(script: "docker inspect -f '{{.State.Running}}' ${containerName} 2>/dev/null || echo 'missing'", returnStdout: true).trim()
    
    switch(containerStatus) {
        case 'true':
            echo "${ANSI_BLUE}✅ Container ${containerName} is already running${ANSI_RESET}"
            break
        case 'false':
            echo "${ANSI_YELLOW}🔄 Starting existing container ${containerName}...${ANSI_RESET}"
            sh "docker start ${containerName}"
            echo "${ANSI_GREEN}✅ Container ${containerName} started successfully${ANSI_RESET}"
            break
        case 'missing':
            echo "${ANSI_RED}🆕 Creating new container ${containerName}...${ANSI_RESET}"
            sh """
            docker run -d -it --name ${containerName} -w ${workdir} \
            -v ${HOST_DIR}/${HOST_USER}/.ssh:${HOST_DIR}/${HOST_USER}/.ssh \
            -v /opt/:/opt/ \
            -v ${WORKSPACE}:${WORKSPACE} \
            -v ${CCACHE_DIR}:${CONTAINER_BASE_WORKDIR}/CCACHE/.ccache \
            -v ${HOST_STORAGE_DIR}${workdir}:${workdir} \
            -v ${base_unpacking_tool_path}/${variant_path}:${CONTAINER_BASE_WORKDIR}/${variant_path} \
            -v /usr/local/bin/:/usr/local/bin/ \
            -e TZ=Asia/Kolkata \
            ${imageName} /bin/bash
            """
            echo "${ANSI_GREEN}✅ Container ${containerName} created and started successfully${ANSI_RESET}"
            break
        default:
            error("❌ Unknown container status: ${containerStatus}")
    }
}

// Enhanced cloning function for AOSP using repo manifests
def cloneOrUpdateAospWithManifest(CONTAINER, MANIFEST_URL, GIT_PATH, MANIFEST_BRANCH, MANIFEST_FILE) {
    echo "${ANSI_BLUE}🔄 Cloning/updating AOSP with repo manifest (AOSP 14)...${ANSI_RESET}"
    
    // Get system cores for optimal parallel sync
    def numCores = sh(returnStdout: true, script: 'nproc').trim()
    def parallelJobs = Math.max(1, (numCores as Integer) - 2)  // Leave 2 cores for system
    
    echo "${ANSI_GREEN}Using ${parallelJobs} parallel jobs for repo sync${ANSI_RESET}"

    // Clean repository if requested
    if (params.CLEAN_REPO == true) {
        echo "${ANSI_YELLOW}🧹 Cleaning repository for fresh clone...${ANSI_RESET}"
        dockerExec("${CONTAINER}", """
            set -xe
            cd ${GIT_PATH}
            # Configure git globally with AOSP 14 optimizations
            git config --global user.name "Jenkins"
            git config --global user.email "jenkins@abcmotor.com"
            git config --global --add safe.directory "*"
            echo "Cleaning repository for fresh clone..."
            rm -rf Unpacking_Tool QWM2290_Android14.0_R03_r002.5_2W
        """)
    }

    // Handle existing local branches
    handleExistingBranches("${CONTAINER}")

    
    // dockerExec("${CONTAINER}", """
    //     set -xe
    //     # Comment __USE_GNU block only if it's not already commented
    //     if [ -f /usr/include/sys/stat.h ]; then
    //         if grep -qE "^[[:space:]]*#ifdef[[:space:]]+__USE_GNU" /usr/include/sys/stat.h && \
    //            ! grep -qE "^[[:space:]]*//[[:space:]]*#ifdef[[:space:]]+__USE_GNU" /usr/include/sys/stat.h; then
    //             echo "Commenting __USE_GNU block in /usr/include/sys/stat.h"
    //             sudo sed -E -i "/^[[:space:]]*#ifdef[[:space:]]+__USE_GNU/,/^[[:space:]]*#endif/ { /^[[:space:]]*\\/\\//! s|^|//| }" /usr/include/sys/stat.h
    //         else
    //             echo "Skip patch: __USE_GNU block already commented or not present"
    //         fi
    //     else
    //         echo "/usr/include/sys/stat.h not found, skipping"
    //     fi
    // """)

    dockerExec("${CONTAINER}", """
        set -xe
        cd ${GIT_PATH}

        # Clean repository if clean repo parameter is set
        #if [ "${params.CLEAN_REPO}" = "true" ]; then
        #    echo "🧹 Cleaning repository for fresh clone..."
        #    rm -rf .repo
        #fi
        
        # Always run repo init with optimizations
        echo "📦 Initializing repo with optimizations..."
        repo init -u ${MANIFEST_URL} -b ${MANIFEST_BRANCH} -m ${MANIFEST_FILE}

        #repo sync -d --force-sync --no-clone-bundle

        #rm -rf QWM2290_Android14.0_R03_r002.5_2W/QSSI.14/vendor/abc/packages/apps

        # Remove ALL local changes (reset and clean) - this is DESTRUCTIVE and intended for CI only!
        echo "⚠️ WARNING: Removing ALL local changes (reset and clean) - this is DESTRUCTIVE and intended for CI only!"
        repo forall -c "git reset --hard HEAD && git clean -ffdx -e out" || true
        
        repo sync -c -d -j${parallelJobs} --force-sync --no-clone-bundle

        # Remove uncommitted changes in the problematic repo directory, but exclude 'out'
        #cd QWM2290_Android14.0_R03_r002.5_2W && git reset --hard && git clean -ffdx -e out || true
        #cd ..

        #rm -rf  QWM2290_Android14.0_R03_r002.5_2W/QSSI.14/vendor/abc/packages/apps/NavigationManager
        #rm -rf QWM2290_Android14.0_R03_r002.5_2W/QSSI.14/vendor/abc/packages/apps/ProfileManager

        # Synchronize the local project directories with the remote repositories
        #repo sync -c -j${parallelJobs} --force-sync --no-clone-bundle --optimized-fetch --prune
        repo sync -c -j${parallelJobs} --force-sync --no-clone-bundle
        
        echo "📥 Handling LFS files..."
        repo forall -c "git lfs pull 2>/dev/null || echo No LFS files in this repo" || true
        
        # Post-sync optimizations
        repo forall -c "git gc --auto" || true
        
        echo "✅ AOSP repository sync complete with optimizations"
        
        # Display sync summary
        echo "📊 Sync Summary:"
        #repo status | head -20
        echo "Total projects synced: \$(repo list | wc -l)"
    """)
}

/**
 * Handle existing local branches
 */
def handleExistingBranches(CONTAINER) {
    dir("${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${CONTAINER}/${BASE_AOSP_DIR}") {
        try {
            dockerExec("${CONTAINER}", """
                set -xe
                
                echo "Detecting existing repo branches..."
                repo branches || true
                
                echo "Discarding any local (uncommitted) changes before abandoning branches..."
                repo forall -c '
                    if [ -n "\$(git status --porcelain)" ]; then
                        echo "Resetting \$REPO_PROJECT (\$REPO_PATH)"
                        git reset --hard
                        git clean -fdx -e out
                    fi
                '
                
                echo "Attempting to abandon all local topic branches..."
                if ! repo abandon --all; then
                    echo "repo abandon reported errors; performing aggressive cleanup fallback..."
                    repo forall -c 'git reset --hard && git clean -ffdx -e out'
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
            echo "${ANSI_YELLOW}⚠️ Warning: Could not process existing branches: ${e.message}${ANSI_RESET}"
        }
    }
}

/**
 * Optimized branch checkout for all components
 */
def checkoutAllBranches(container) {
    echo "${ANSI_BLUE}🌿 Checking out branches...${ANSI_RESET}"
    
    def branchConfigs = [
        [dir: BASE_AOSP_DIR, branch: params.BASE_AOSP_TAG_OR_BRANCH_NAME, name: "QWM2290_Android14.0_R03_r002.5_2W"],
        [dir: BTConnectionManager_DIR, branch: params.BTConnectionManager, name: "btservicemanagerapk"],
        [dir: NavigationManager_DIR, branch: params.NavigationManager, name: "navigationmanagerapk"],
        [dir: ProfileManager_DIR, branch: params.ProfileManager, name: "profilemanagerapk"],
        [dir: SystemHealthMonitor_DIR, branch: params.SystemHealthMonitor, name: "systemhealthmonitorapk"],
        [dir: AudioServiceManager_DIR, branch: params.AudioServiceManager, name: "abcaudioservicemanagerapk"],
        [dir: OtaManager_DIR, branch: params.OtaManager, name: "otamanagerapk"],
        [dir: CODPAPIManager_DIR, branch: params.CodPapiManager, name: "codpapimanagerapk"],
        [dir: WifiManager_DIR, branch: params.WifiManager, name: "wifimanagerapk"],
        [dir: MediaServiceManager_DIR, branch: params.MediaServiceManager, name: "mediaservicemanagerapk"],
        [dir: TwoWheelerService_DIR, branch: params.TwoWheelerService, name: "twowheelerserviceapk"],
        [dir: TelephonyManager_DIR, branch: params.TelephonyManager, name: "telephonymanagerapk"],
        [dir: Projection_DIR, branch: params.Projection, name: "aaprojectionapk"],
        [dir: ThinkSeedRui_DIR, branch: params.ThinkSeedRui, name: "ThinkSeedRui"],
        [dir: ThinkWifi_DIR, branch: params.ThinkWifi, name: "ThinkWifi"],
        [dir: ABCHMIClientapk_DIR, branch: params.ABCHMIClientapk, name: "ABCHMIClientapk"],
        [dir: ThinkCarPlay_DIR, branch: params.ThinkCarPlay, name: "ThinkCarPlay"]
    ]
    
    branchConfigs.each { config ->
        if (config.branch?.trim()) {
            echo "${ANSI_BLUE}Checking out ${config.name}: ${config.branch} on ${container} ${ANSI_RESET}"
            checkoutBranchOptimized(container, config.dir, config.branch)
        } else {
            echo "${ANSI_YELLOW}Skipping ${config.name} - no branch specified${ANSI_RESET}"
        }
    }
    
    echo "${ANSI_GREEN}✅ All branches checked out successfully${ANSI_RESET}"
}

/**
 * Optimized branch checkout with error handling
 */
/**
 * Checks out the specified branch, tag, or commit in the given project path inside the specified Docker container.
 *
 * @param container      The name of the Docker container where the checkout should occur.
 * @param projectPath    The path to the project inside the container.
 * @param branch         The branch, tag, or commit to checkout.
 * 
 * This function attempts to checkout the provided ref, pulls latest changes if it's a branch,
 * and handles Git LFS files if present. Throws an error if checkout fails.
 */
def checkoutBranchOptimized(container, projectPath, branch) {
    sh """
        echo "${ANSI_BLUE}Checking out ${projectPath}: ${branch} on ${container} ${ANSI_RESET}"
        docker exec ${container} bash -c '
            set -xe

            cd ${projectPath}
            pwd
            echo "Checking out branch ${branch} for ${projectPath}"

            # Fetch latest changes
            git fetch --all --prune

            # Checkout ref (branch, tag, or commit)
            git checkout ${branch} || { echo "Failed to checkout ${branch}"; exit 1; }

            # Check if checked out ref is a branch and remote github exists
            if git branch -a | grep -qE "(remotes/github/${branch}|\\s${branch}\$)"; then
                if git remote | grep -q "^github\$"; then
                    echo "Pulling latest changes for branch ${branch}"
                    git pull github ${branch} 2>&1 || { echo "git pull failed with error:"; git pull github ${branch} 2>&1; }
                else
                    echo "Remote 'github' does not exist, skipping pull"
                fi
            else
                echo "${branch} is a tag or commit, skipping pull"
            fi
            
            # Handle LFS files if present
            git lfs pull 2>/dev/null || echo "No LFS files to pull"
            
            echo "✅ Successfully checked out ${branch}"
        '
    """
}

def initializeEnvironmentVariables() {

    if(params.Factory) {
        FACTORY_PREFIX = 'Factory'
        STORAGE_PATH = "file://storage/emulated/0/Download/FactoryBinaries/E4/msm8953_64-ota-${BUILD_NUMBER}_${RC_NAME}.zip"
    } else {
        FACTORY_PREFIX = 'General'
        STORAGE_PATH = "file://storage/emulated/0/Download/VerifiedBinary/E4/msm8953_64-ota-${BUILD_NUMBER}_${RC_NAME}.zip"
    }
}

// This function defines to generate fastboot images with AOSP 14 optimizations
def FastbootImagesGeneration(CONTAINER, BUILD_NUMBER, BUILD_VARIANT) {
    echo "${ANSI_RED}🚀 Starting FastbootImagesGeneration for AOSP 14${ANSI_RESET}"
    
    // Get system cores for optimal parallel jobs
    def numCores = sh(returnStdout: true, script: 'nproc').trim()
    def parallelJobs = Math.max(1, (numCores as Integer) - 2)  // Leave 2 cores for system
    
    echo "${ANSI_GREEN}Using ${parallelJobs} parallel jobs out of ${numCores} available cores${ANSI_RESET}"
    
    sh """
        echo "${ANSI_RED}Starting Build (Variant: ${BUILD_VARIANT})${ANSI_RESET}"
        docker exec ${CONTAINER} bash -c "
            set -xe
            cd ${BASE_AOSP_DIR} && pwd

            # Export AOSP 14 specific optimizations
            export USE_CCACHE=1
            export CCACHE_EXEC=/usr/bin/ccache
            export CCACHE_DIR=../CCACHE/.ccache
            export CCACHE_SLOPPINESS=file_macro,locale,time_macros
            export WITH_DEXPREOPT=true
            export SOONG_PARALLEL_JOBS=${parallelJobs}
            export NINJA_STATUS='[%f/%t] '
            export JACK_SERVER_VM_ARGUMENTS='-Dfile.encoding=UTF-8 -XX:+TieredCompilation -Xmx4g'
            export ASAN_OPTIONS='detect_leaks=0:halt_on_error=1:abort_on_error=1'
            export MALLOC_CHECK_=0

            # Configure ccache for optimal performance
            ccache -M 150G
            ccache -o compression=true
            ccache -o compression_level=6
            ccache -o max_files=0
            ccache -s

            # Execute build with optimizations
            if [ "${BUILD_VARIANT}" = "user" ]; then
                bash build_qwm2290.sh --user
            elif [ "${BUILD_VARIANT}" = "userdebug" ]; then
                bash build_qwm2290.sh --all
            else
                bash build_qwm2290.sh --user
            fi

            # Package fastboot images
            cd ${ABC_TARGET_PRODUCT_DIR}/${AOSP_OUT_DIR}
            zip -r fastboot.zip *.img *.elf
            ls -la

            echo '✅ Build (${BUILD_VARIANT}) completed successfully'
        "
    """
    
    BINARY_SUFFIX = "${RC_NAME}_${BUILD_VARIANT}_#${BUILD_NUMBER}"

    sh """
        echo "${ANSI_RED}Copying FASTBOOT images to Release DIR.${ANSI_RESET}"
        set -xe
        cd "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${CONTAINER}/${BASE_AOSP_DIR}"
        cd ${ABC_TARGET_PRODUCT_DIR}/${AOSP_OUT_DIR}
        pwd
        ls
        mv fastboot.zip fastboot_${BINARY_SUFFIX}.zip
        aws s3 cp "fastboot_${BINARY_SUFFIX}.zip" s3://abcm-rnd-s3-uat01/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AWS_SOFTWARE_RELEASE_BUILD_DIR}/ 1>/dev/null
        n=0; until [ \$n -ge 5 ]; do sudo azcopy copy "fastboot_${BINARY_SUFFIX}.zip" 'https://${AZURE_STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AZURE_SOFTWARE_RELEASE_BUILD_DIR}/?${SAS_KEY_INT}' --recursive && break; n=\$((n+1)); sleep 5; done
        echo "Attempting fallback upload via SSH/SCP..."
        REMOTE_DIR="/home/swcocuser/BINARIES/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${SOFTWARE_RELEASE_BUILD_DIR}"
        # ssh -o StrictHostKeyChecking=no swcocuser@xx.yy.xx "mkdir -p \"\${REMOTE_DIR}\""
        # if scp -o StrictHostKeyChecking=no "fastboot_${BINARY_SUFFIX}.zip" "swcocuser@xx.yy.zz:\${REMOTE_DIR}/"; then
        #     echo "✅ Fallback SCP upload successful"
        # else
        #     echo "❌ Fallback SCP upload failed"
        #     exit 1
        # fi
    """
}

// This function defines to generate QFIL images 
def QFILGeneration(CONTAINER, UNPACKING_TOOL_DIR, BUILD_NUMBER) {
    sh """
        echo "${ANSI_RED}Starting QFIL Generation ${ANSI_RESET}"
        docker exec -w ${CONTAINER_BASE_WORKDIR}/${CONTAINER}/${BASE_AOSP_DIR} ${CONTAINER} bash -c "
        set -xe &&
        exportSE_CCACHE=1 &&
        export CCACHE_EXEC=/usr/bin/ccache &&
        export CCACHE_DIR=../CCACHE/.ccache &&
        ccache -M 100G &&
        pwd &&
        rm -rf ../Unpacking_Tool/qfil_download_emmc &&
        bash build_qwm2290.sh --qfil
        cd ../Unpacking_Tool
        rm -rf *.zip &&
        zip -r emmc.zip qfil_download_emmc
        "
    """
    BINARY_SUFFIX = "${RC_NAME}_${BUILD_VARIANT}_#${BUILD_NUMBER}"
    sh """
        echo "${ANSI_RED}Copying QFIL images to Release DIR.${ANSI_RESET}"
        set -xe
        # cd ${AOSP_UNPACKING_TOOL_PATH}/${UNPACKING_TOOL_DIR}
        cd ${HOST_STORAGE_DIR}/${CONTAINER_BASE_WORKDIR}/${CONTAINER}/Unpacking_Tool
        pwd
        ls
        mv emmc.zip "emmc_${BINARY_SUFFIX}.zip"
        aws s3 cp "emmc_${BINARY_SUFFIX}.zip" s3://abcm-rnd-s3-uat01/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AWS_SOFTWARE_RELEASE_BUILD_DIR}/ 1>/dev/null
        n=0; until [ \$n -ge 5 ]; do sudo azcopy copy "emmc_${BINARY_SUFFIX}.zip" 'https://${AZURE_STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AZURE_SOFTWARE_RELEASE_BUILD_DIR}/?${SAS_KEY_INT}' --recursive && break; n=\$((n+1)); sleep 5; done
        echo "Attempting fallback upload via SSH/SCP..."
        REMOTE_DIR="/home/swcocuser/BINARIES/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${SOFTWARE_RELEASE_BUILD_DIR}"
        # ssh -o StrictHostKeyChecking=no swcocuser@10.121.4.232 "mkdir -p \"\${REMOTE_DIR}\""
        # if scp -o StrictHostKeyChecking=no "emmc_${BINARY_SUFFIX}.zip" "swcocuser@10.121.4.232:\${REMOTE_DIR}/"; then
        #     echo "✅ Fallback SCP upload successful"
        # else
        #     echo "❌ Fallback SCP upload failed"
        #     exit 1
        # fi
    """
}

// This function defines to generate OTA images
def OTAGeneration(CONTAINER, BUILD_VARIANT, STORAGE_PATH, BUILD_NUMBER) {
    sh """
        echo "${ANSI_RED}Starting OTA Generation ${ANSI_RESET}"
        docker exec ${CONTAINER} bash -c '
        set -xe &&
        export BUILD_NUMBER=${BUILD_NUMBER} &&
        export RC_NAME=${RC_NAME} &&
        export USE_CCACHE=1 &&
        export CCACHE_EXEC=/usr/bin/ccache &&
        export CCACHE_DIR=../CCACHE/.ccache &&
        ccache -M 100G &&
        pwd &&
        rm -rf configs* &&
        rm -rf target*.zip &&
        rm -rf dist_output dist* &&
        rm -rf OTA* &&
        source build/envsetup.sh &&
        lunch msm8953_64-${BUILD_VARIANT} &&
        make dist DIST_DIR=dist_output &&
        mkdir -p configs &&
        mv dist_output/msm8953_64-ota-${BUILD_NUMBER}.zip dist_output/msm8953_64-ota-${BUILD_NUMBER}_${RC_NAME}.zip &&
        mv dist_output/msm8953_64-target_files-${BUILD_NUMBER}.zip dist_output/full_OTA.zip &&
        PYTHONPATH=\${ANDROID_BUILD_TOP}/build/make/tools/releasetools:\${PYTHONPATH} &&
        python gen_update_config.py \
        --ab_install_type=NON_STREAMING \
        --ab_force_switch_slot \
        dist_output/msm8953_64-ota-${BUILD_NUMBER}_${RC_NAME}.zip  \
        configs/msm8953_64-ota-${BUILD_NUMBER}_${RC_NAME}.json \
        ${STORAGE_PATH} &&
        zip -r target.zip configs &&
        zip -uj target.zip dist_output/msm8953_64-ota-${BUILD_NUMBER}_${RC_NAME}.zip
        '
    """
    BINARY_SUFFIX = "${ENVIRONMENT}_${FACTORY_PREFIX}_${RC_NAME}_${BUILD_VARIANT}-${BUILD_NUMBER}"
    sh """
        echo "${ANSI_RED}Copying OTA images to Release DIR.${ANSI_RESET}"
        set -xe
        cd "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${CONTAINER}"
        pwd
        ls -la
        sudo mv target.zip "OTA_${BINARY_SUFFIX}.zip"
        aws s3 cp "OTA_${BINARY_SUFFIX}.zip" s3://abcm-rnd-s3-uat01/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AWS_SOFTWARE_RELEASE_BUILD_DIR}/ 1>/dev/null
        n=0; until [ \$n -ge 5 ]; do sudo azcopy copy "OTA_${BINARY_SUFFIX}.zip" 'https://${AZURE_STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AZURE_SOFTWARE_RELEASE_BUILD_DIR}/?${SAS_KEY_INT}' --recursive && break; n=\$((n+1)); sleep 5; done
        # scp OTA_${BINARY_SUFFIX}.zip aeguser@10.121.4.109:/data/binary/WR_south
        echo "Attempting fallback upload via SSH/SCP..."
        REMOTE_DIR="/home/swcocuser/BINARIES/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${SOFTWARE_RELEASE_BUILD_DIR}"
        # ssh -o StrictHostKeyChecking=no swcocuser@10.121.4.232 "mkdir -p \"\${REMOTE_DIR}\""
        # if scp -o StrictHostKeyChecking=no "OTA_${BINARY_SUFFIX}.zip" "swcocuser@10.121.4.232:\${REMOTE_DIR}/"; then
        #     echo "✅ Fallback SCP upload successful"
        # else
        #     echo "❌ Fallback SCP upload failed"
        #     exit 1
        # fi
    """
}

def aospBuild(envVars) {
    def startTime = System.currentTimeMillis()
    try {
        echo "${ANSI_PURPLE}🚀 Starting ${envVars.stageName} for AOSP 14${ANSI_RESET}"

        // Check system resources before build
        checkSystemResources(envVars.container)

        // Cloning AOSP source code into Jenkins workspace
        // cloneOrUpdateGitRepository(envVars.container, "${AOSP_PROJECT_URL}", "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${envVars.container}", "${AOSP_TAG}")

        // Setting BUILD_VARIANT
        BUILD_VARIANT = envVars.buildVariant

        // sh """
        // docker cp /data/tool/AOSP_A12/a12_build.sh ${envVars.container}:${CONTAINER_BASE_WORKDIR}/${envVars.container}/${envVars.sourceDir}
        // """

        // Entering into AOSP workspace
        echo "${ANSI_GREEN}BUILD_VARIANT: ${BUILD_VARIANT}${ANSI_RESET}"
        echo "${ANSI_GREEN}Container: ${envVars.container}${ANSI_RESET}"
        echo "${ANSI_GREEN}Unpacking Tool: ${envVars.unpackingTool}${ANSI_RESET}"

        dir("${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${envVars.container}/${BASE_AOSP_DIR}") {
            // Removing Out Directory based on input from environment variables
            if (params.CLEAN_BUILD) {
                echo "${ANSI_RED}🧹 Performing clean build - removing output directory${ANSI_RESET}"
                sh """
                    sudo rm -rf QSSI.14/out
                    sudo rm -rf UM.9.15/out
                    echo "✅ Clean build setup completed"
                """
            } else {
                echo "${ANSI_GREEN}📈 Incremental build - preserving existing artifacts${ANSI_RESET}"
            }

            // def swVersionFile = "${ABC_TARGET_PRODUCT_DIR}/device/qcom/bengal_2w/SoftwareVersion.txt"
            // sh """
            //     set -xe
            //     pwd
            //     echo "${RC_NAME}" > "${swVersionFile}"
            //     cat "${swVersionFile}"
            // """

            //cat "${swVersionFile}"

            // Monitor build start
            echo "${ANSI_BLUE}📊 Build Progress Monitoring Started${ANSI_RESET}"
            
            // Generating Fastboot images with optimizations
            FastbootImagesGeneration("${envVars.container}", "${BUILD_NUMBER}", "${BUILD_VARIANT}")

            // Generating QFIL Images
            QFILGeneration("${envVars.container}", "${envVars.unpackingTool}", "${BUILD_NUMBER}")

            // Generate OTA images (if enabled)
            if (params.GENERATE_OTA) {
                OTAGeneration("${envVars.container}", "${BUILD_VARIANT}", "${STORAGE_PATH}", "${BUILD_NUMBER}")
            }
        }

        // Calculate build time
        def endTime = System.currentTimeMillis()
        def buildDuration = (endTime - startTime) / 1000 / 60 // in minutes
        echo "${ANSI_GREEN}✅ ${envVars.stageName} completed successfully in ${buildDuration} minutes${ANSI_RESET}"
        
        // Log build statistics
        logBuildStatistics(envVars.container, buildDuration)
        
    } catch (Exception e) {
        def endTime = System.currentTimeMillis()
        def buildDuration = (endTime - startTime) / 1000 / 60 // in minutes
        
        echo "${ANSI_RED}❌ FAILED: ${envVars.stageName} failed after ${buildDuration} minutes${ANSI_RESET}"
        echo "${ANSI_RED}Error details: ${e.message}${ANSI_RESET}"
        
        // Collect failure logs
        collectFailureLogs(envVars.container)
        
        currentBuild.result = 'FAILURE'
        FAILED_STAGE = envVars.stageName
        error("ERROR at ${envVars.stageName} stage: ${e.message}")
    }
}

// Installing Packages on Container if not already installed
def installingPackagesOnDocker(CONTAINER) {
    sh """
    docker exec ${CONTAINER} bash -c '
    sudo apt-get update 1>/dev/null &&
    sudo apt-get install -y vim 1>/dev/null &&
    sudo apt-get install -y sudo 1>/dev/null &&
    sudo apt-get install -y git-lfs 1>/dev/null &&
    sudo apt-get install -y ccache 1>/dev/null &&
    sudo apt-get install -y wkhtmltopdf 1>/dev/null
    '
    """
}

/**
 * Check system resources before starting build
 */
def checkSystemResources(container) {
    echo "${ANSI_BLUE}🔍 Checking system resources for ${container}${ANSI_RESET}"
    
    sh """
    echo "=== System Resource Check ==="
    echo "Available Memory:"
    free -h
    echo ""
    echo "Available Disk Space:"
    df -h
    echo ""
    echo "CPU Information:"
    nproc --all
    echo ""
    echo "Load Average:"
    uptime
    echo ""
    """
    
    // Check container resources
    sh """
    echo "=== Container Resource Check ==="
    docker exec ${container} bash -c '
        echo "Container Memory Usage:"
        free -h
        echo ""
        echo "Container Disk Usage:"
        df -h
        echo ""
        echo "CCACHE Status:"
        ccache -s 2>/dev/null || echo "CCACHE not configured yet"
    '
    """
}

/**
 * Log build statistics after completion
 */
def logBuildStatistics(container, buildDuration) {
    echo "${ANSI_BLUE}📊 Collecting build statistics${ANSI_RESET}"
    
    sh """
    echo "=== Build Statistics ==="
    echo "Build Duration: ${buildDuration} minutes"
    echo "Build Timestamp: \$(date)"
    echo ""
    """
    
    // Collect CCACHE statistics
    sh """
    docker exec ${container} bash -c '
        echo "=== CCACHE Statistics ==="
        ccache -s || echo "CCACHE statistics not available"
        echo ""
        echo "=== Final Disk Usage ==="
        df -h
    '
    """
}

/**
 * Collect failure logs for debugging
 */
def collectFailureLogs(container) {
    echo "${ANSI_RED}🔍 Collecting failure logs for debugging${ANSI_RESET}"
    
    try {
        sh """
        echo "=== Build Failure Analysis ==="
        echo "Timestamp: \$(date)"
        echo "Container: ${container}"
        echo ""
        
        # Collect system state
        echo "=== System State ==="
        free -h
        df -h
        uptime
        echo ""
        
        # Check container logs
        echo "=== Container State ==="
        docker exec ${container} bash -c '
            echo "Working Directory Contents:"
            pwd && ls -la
            echo ""
            echo "Build Log Tail (if exists):"
            #find . -name "*.log" -exec tail -50 {} \\; 2>/dev/null || echo "No log files found"
        ' || echo "Container not accessible"
        """
    } catch (Exception e) {
        echo "${ANSI_RED}Could not collect all failure logs: ${e.message}${ANSI_RESET}"
    }
}

// This Function is useful for fecthing the detailed logs of bewteen the tags. 
//i.e commit wise logs/changes ,total file changes and addtions/deletions
def formatGitLogOutput(logOutput) {
    def commits = []
    def currentCommit = [:]
    def inFileChanges = false
    def totalFilesChanged = 0
    def totalInsertions = 0
    def totalDeletions = 0
    def elmIDs = [] // Store all extracted numbers (ELM IDs)

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
        } else if (line.startsWith("    ")) { // Assuming the commit message indentation is four spaces
            currentCommit.message = (currentCommit.message ?: "") + line.trim() +"<hr>"
            //Extract the ELM ID's from the commit message (4 to 8 digits)
            def numberMatches = (currentCommit.message ?: "").findAll(/\b\d{4,8}\b/)
            // Add all matches to elmIDs
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

    // Construct HTML table header
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
    // Add rows to HTML table
    commits.each { commit ->
        // Ensure commit message and file changes are not null
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

    // Close HTML table
    htmlTable += "</table>"
    return [htmlTable, elmIDs]
}

// This Function is useful for fecthing the summary of bewteen the tags. i.e total file changes and addtions/deletions
def calculateAndSendDiff(gitDiff, logStatOutput_short) {
    // Use 'git diff' to get information about modified files and changes
    def tagchanges = "<table border='1'><tr><th>Affected Files</th><th>Additions</th><th>Deletions</th></tr>"
    
    def changes = gitDiff.split('\n').collect { line ->
        echo "Processing line: ${line}"
        def parts = line.split('\t')

        if (parts.size() < 3) {
            echo "Skipping invalid line: ${line}"
            return null // Skip this entry
        }

        def additions, deletions, file

        try {
            additions = parts[0].toInteger()
        } catch (NumberFormatException e) {
            echo "Error parsing additions: ${e.message}"
            additions = 0
        }

        try {
            deletions = parts[1].toInteger()
        } catch (NumberFormatException e) {
            echo "Error parsing deletions: ${e.message}"
            deletions = 0
        }

        file = parts[2]
        return [file: file, additions: additions, deletions: deletions]
    }.findAll { it != null } // Remove any null values


    // Display the changes
    echo "Modified Files and Changes between the tags:"
    if (!changes || changes.isEmpty()) {
        echo "No changes detected."
    } else {
    echo "Modified Files and Changes between the tags:"
    changes.each { change ->
        tagchanges += "<tr>"
        tagchanges += "<td>${change.file}</td>"
        tagchanges += "<td>${change.additions}</td>"
        tagchanges += "<td>${change.deletions}</td>"
        tagchanges += "</tr>"
            echo "File: ${change.file}, Additions: ${change.additions}, Deletions: ${change.deletions}"
        }
    }

    // Extracting relevant details using regex for shortstat
    def shortstatDetails = [:]
    try {
        echo "logStatOutput_short: ${logStatOutput_short}"

    def parts = logStatOutput_short.split(",")
        if (parts.size() < 3) {
            throw new Exception("Invalid shortstat output format: ${logStatOutput_short}")
        }

    shortstatDetails.totalFilesChanged = parts[0].trim().tokenize()[0]
    shortstatDetails.totalInsertions = parts[1].trim().tokenize()[0]
    shortstatDetails.totalDeletions = parts[2].trim().tokenize()[0]

    echo "shortstatDetails: ${shortstatDetails}"
        
    tagchanges += """
    <tr>
            <td style="color:blue">Total Files Changed: <b>${shortstatDetails.totalFilesChanged}</b></td>
    <td style="color:green">Total Additions: <b>${shortstatDetails.totalInsertions}</b></td>
    <td style="color:purple">Total Deletions: <b>${shortstatDetails.totalDeletions}</b></td>
    </tr>
    """
    } catch (Exception e) {
        echo "Error occurred while processing shortstat details: ${e.getMessage()}"
        shortstatDetails.totalFilesChanged = "N/A"
        shortstatDetails.totalInsertions = "N/A"
        shortstatDetails.totalDeletions = "N/A"
        tagchanges += """
        <tr>
            <td style="color:red">Error processing shortstat details</td>
        </tr>
        """
    }
    tagchanges += "</table>"
    // Send an email with the changes
    return tagchanges
}

// Define the function to check if the provided input is a branch or a tag
def checkGitRefType(CONTAINER, String refName, String prevRefName) {
    script {
        dir("${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${CONTAINER}/${BASE_AOSP_DIR}") {
            sh """
                git config --global --add safe.directory  "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${CONTAINER}"
            """
            
            def branchOutput = ''
            def tagOutput = ''
            
            try {
                branchOutput = sh(script: """
                                        git for-each-ref --format='%(refname:short)' refs/heads | grep '^${refName}\$'
                                        """, returnStdout: true).trim()
            } catch (Exception e) {
                // No need to handle here, as grep not finding anything is not an error in this context
            }
                
            try {
                tagOutput = sh(script: """
                git for-each-ref --format='%(refname:short)' refs/tags | grep '^${refName}\$'
                """, returnStdout: true).trim()
            } catch (Exception e) {
                // No need to handle here, as grep not finding anything is not an error in this context
            }
            
            if (branchOutput) {
                echo "Will provide git diff between two commits"
                // Get the latest two commit IDs
                def commitIDs = sh(script: """git log -n 3 --pretty=format:'%h'""", returnStdout: true).trim().split('\n')
        
                // Assign the commit IDs to separate variables
                ref1 = commitIDs.size() > 0 ? commitIDs[0] : ''
                def commitID2 = commitIDs.size() > 1 ? commitIDs[1] : ''
                ref2 = commitIDs.size() > 1 ? commitIDs[2] : ''
            } else if (tagOutput) {
                echo "Will provide git diff between two Tags "
                // Fetching info for PREVIOUS_RELEASED_TAG_NAME 
                echo "PREVIOUS_RELEASED_TAG_NAME is Empty."
                ref1 = "${refName}"
                ref2 = sh(script: "git tag --sort=-creatordate --merged ${refName}^ | sed -n 1p", returnStdout: true).trim()
                //PREVIOUS_RELEASED_TAG_NAME = ref2
            } else {
                echo "Will provide git diff between two commits"
                // Get the latest two commit IDs
                def commitIDs = sh(script: """git log -n 3 --pretty=format:'%h'""", returnStdout: true).trim().split('\n')
        
                // Assign the commit IDs to separate variables
                ref1 = commitIDs.size() > 0 ? commitIDs[0] : ''
                def commitID2 = commitIDs.size() > 1 ? commitIDs[1] : ''
                ref2 = commitIDs.size() > 1 ? commitIDs[2] : ''
            }

            TAG_NAME = "${ref1}"

            if (prevRefName == null || prevRefName.isEmpty()) {
                println("PREVIOUS_RELEASED_TAG_NAME variable is empty or null")
                PREVIOUS_RELEASED_TAG_NAME = "${ref2}"
            } else {
                println("PREVIOUS_RELEASED_TAG_NAME variable is NOT empty")
                PREVIOUS_RELEASED_TAG_NAME = "${prevRefName}"
            }
            
            echo "${ANSI_PURPLE}Latest Ref1(TAG_NAME):${TAG_NAME}${ANSI_RESET}"
            echo "${ANSI_GREEN}Second latest Ref2(PREVIOUS_RELEASED_TAG_NAME):${PREVIOUS_RELEASED_TAG_NAME}${ANSI_RESET}"

            gitDiff = sh(script: "git diff ${PREVIOUS_RELEASED_TAG_NAME}..${TAG_NAME} --numstat", returnStdout: true).trim()
            logStatOutput = sh(script: "git log ${PREVIOUS_RELEASED_TAG_NAME}..${TAG_NAME} --numstat",returnStdout: true).trim()
            logStatOutput_short = sh(script: "git diff ${PREVIOUS_RELEASED_TAG_NAME}..${TAG_NAME} --shortstat",returnStdout: true).trim()

            // Parse and format the output
            def (tagchanges_stat, elmIDs) = formatGitLogOutput(logStatOutput.split("\\n"))
            diffchanges = calculateAndSendDiff(gitDiff, logStatOutput_short)

            return [TAG_NAME, PREVIOUS_RELEASED_TAG_NAME, tagchanges_stat, diffchanges, gitDiff, logStatOutput_short, elmIDs]
        }
    }
}

// Defined the function to fetch change log between the dates for AOSP and Cluster
def aospChangeLogForDailyBuilds(CONTAINER, String refName, TODAY_DATE, YESTERDAY_DATE) {
    script {
        dir("${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${CONTAINER}/${refName}") {
            sh """
                git config --global --add safe.directory  "${HOST_STORAGE_DIR}${CONTAINER_BASE_WORKDIR}/${CONTAINER}"
            """

            TAG_NAME = "${TODAY_DATE}"
            PREVIOUS_RELEASED_TAG_NAME = "${YESTERDAY_DATE} 00:00"

            println("Latest Ref1: ${TAG_NAME}")
            println("Second latest Ref2: ${PREVIOUS_RELEASED_TAG_NAME}")

            //def gitDiff = sh(script: "git diff ${PREVIOUS_RELEASED_TAG_NAME}..${TAG_NAME} --numstat", returnStdout: true).trim()
            logStatOutput = sh(script: "git log --since='${PREVIOUS_RELEASED_TAG_NAME}' --until='${TAG_NAME}' --numstat",returnStdout: true).trim()
            COMMIT_COUNT = sh(script: "git rev-list --count --since='${PREVIOUS_RELEASED_TAG_NAME}' --until='${TAG_NAME}' HEAD",returnStdout: true).trim() ?: "0"

            // Parse and format the output
            def (tagchanges_stat, elmIDs) = formatGitLogOutput(logStatOutput.split("\\n"))
            //def diffchanges = calculateAndSendDiff(gitDiff, logStatOutput_short)

            return [TAG_NAME, PREVIOUS_RELEASED_TAG_NAME, tagchanges_stat, COMMIT_COUNT, elmIDs]
        }
    }
}

// Defined the function to fetch change log between the dates for OTA Client and PKI
def changeLogForDailyBuilds(CONTAINER, String refName, TODAY_DATE, YESTERDAY_DATE) {
    script {
        dir("${WORKSPACE}/${CONTAINER}") {
            sh """
                git config --global --add safe.directory  "${WORKSPACE}/${CONTAINER}"
            """

            TAG_NAME = "${TODAY_DATE}"
            PREVIOUS_RELEASED_TAG_NAME = "${YESTERDAY_DATE} 00:00"

            println("Latest Ref1: ${TAG_NAME}")
            println("Second latest Ref2: ${PREVIOUS_RELEASED_TAG_NAME}")

            def logStatOutput = sh(script: "git log --since='${PREVIOUS_RELEASED_TAG_NAME}' --until='${TAG_NAME}' --numstat",returnStdout: true).trim()
            COMMIT_COUNT = sh(script: "git rev-list --count --since='${PREVIOUS_RELEASED_TAG_NAME}' --until='${TAG_NAME}' HEAD",returnStdout: true).trim() ?: "0"

            // Parse and format the output
            def (tagchanges_stat, elmIDs) = formatGitLogOutput(logStatOutput.split("\\n"))

            return [TAG_NAME, PREVIOUS_RELEASED_TAG_NAME, tagchanges_stat, COMMIT_COUNT, elmIDs]
        }
    }
}

// Method to replace placeholders in the HTML template
def replacePlaceholders(htmlTemplate, variables) {
    variables.each { key, value ->
        //echo "Key: ${key} , Value: ${value}"
        htmlTemplate = htmlTemplate.replaceAll(/\$\{${key}\}/, value)
    }
    return htmlTemplate
}

/**
 * Enhanced success handler with comprehensive reporting
 */
def handleSuccess() {
    echo "${ANSI_GREEN}🎉 Pipeline succeeded! Preparing notifications...${ANSI_RESET}"
    
    script {
        try {

            currentBuild.displayName = "${SOFTWARE_RELEASE_BUILD_DIR}"
            currentBuild.description = """
            AOSP_TAG: <b>${BASE_AOSP_TAG_OR_BRANCH_NAME}</b> <br>
            """
            AOSP_SW_VERSION = "${RC_NAME}"
            // Prepare build details
            def buildDetails = prepareBuildDetails()

            echo "Build Details: ${buildDetails}"
            
            // Generate change logs for all meta projects
            def changeLogResults = generateChangeLogsForAllProjects()

            echo "Change Log Results: ${changeLogResults}"
            
            // Process ELM IDs and create links
            def elmProcessingResults = processElmIds(changeLogResults.elmIDs)

            echo "ELM Processing Results: ${elmProcessingResults}"
            
            // Generate email content
            def emailContent = generateEmailContent(buildDetails, changeLogResults.variables, elmProcessingResults)
            
            // Send email notification
            sendEmailNotification(emailContent)
            
            // Update Django database if enabled
            //updateDjangoDatabase(buildDetails, elmProcessingResults.DATABASE_ELM_ID)
            
            echo "${ANSI_GREEN}✅ Success handling completed${ANSI_RESET}"
            
        } catch (Exception e) {
            echo "${ANSI_YELLOW}⚠️ Warning: Success handling encountered an error: ${e.message}${ANSI_RESET}"
        }
    }
}

/**
 * Prepare build details for reporting
 */
def prepareBuildDetails() {
    return [
        todayDate: TODAY_DATE ?: '',
        yesterdayDate: YESTERDAY_DATE ?: '',
        date: DATE ?: '',
        buildNumber: BUILD_NUMBER,
        workspace: WORKSPACE,
        buildNumber: "${BUILD_NUMBER}",
        ABC_MANIFEST_BRANCH_OR_TAG: "${ABC_MANIFEST_BRANCH_OR_TAG}",
        MANIFEST_FILE: "${MANIFEST_FILE}",
        AOSP_TAG: QWM2290_ANDROID14_0_R03_R002_5_2W_MANIFEST_BRANCH_OR_REVISION ?: '',
        AOSP_TAG_COMMIT_ID_USER: QWM2290_ANDROID14_0_R03_R002_5_2W_CURRENT_REVISION ?: '',
        //APK component branch & commit mappings (flattened, consistent, minimal)
        btservicemanagerapk_BRANCH:        BTSERVICEMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        btservicemanagerapk_COMMIT_ID:     BTSERVICEMANAGERAPK_CURRENT_REVISION ?: '',
        navigationmanagerapk_BRANCH:       NAVIGATIONMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        navigationmanagerapk_COMMIT_ID:    NAVIGATIONMANAGERAPK_CURRENT_REVISION ?: '',
        profilemanagerapk_BRANCH:          PROFILEMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        profilemanagerapk_COMMIT_ID:       PROFILEMANAGERAPK_CURRENT_REVISION ?: '',
        systemhealthmonitorapk_BRANCH:     SYSTEMHEALTHMONITORAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        systemhealthmonitorapk_COMMIT_ID:  SYSTEMHEALTHMONITORAPK_CURRENT_REVISION ?: '',
        abcaudioservicemanagerapk_BRANCH:  ABCAUDIOSERVICEMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        abcaudioservicemanagerapk_COMMIT_ID: ABCAUDIOSERVICEMANAGERAPK_CURRENT_REVISION ?: '',
        otamanagerapk_BRANCH:              OTAMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        otamanagerapk_COMMIT_ID:           OTAMANAGERAPK_CURRENT_REVISION ?: '',
        codpapimanagerapk_BRANCH:          CODPAPIMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        codpapimanagerapk_COMMIT_ID:      CODPAPIMANAGERAPK_CURRENT_REVISION ?: '',
        wifimanagerapk_BRANCH:             WIFIMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        wifimanagerapk_COMMIT_ID:          WIFIMANAGERAPK_CURRENT_REVISION ?: '',
        mediaservicemanagerapk_BRANCH:     MEDIASERVICEMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        mediaservicemanagerapk_COMMIT_ID:  MEDIASERVICEMANAGERAPK_CURRENT_REVISION ?: '',
        twowheelerserviceapk_BRANCH: TWOWHEELERSERVICEAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        twowheelerserviceapk_COMMIT_ID: TWOWHEELERSERVICEAPK_CURRENT_REVISION ?: '',
        telephonymanagerapk_BRANCH:        TELEPHONYMANAGERAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        telephonymanagerapk_COMMIT_ID:     TELEPHONYMANAGERAPK_CURRENT_REVISION ?: '',
        aaprojectionapk_BRANCH:          AAPROJECTIONAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        aaprojectionapk_COMMIT_ID:       AAPROJECTIONAPK_CURRENT_REVISION ?: '',
        thinkseedrui_BRANCH: THINKSEEDRUI_MANIFEST_BRANCH_OR_REVISION ?: '',
        thinkseedrui_COMMIT_ID: THINKSEEDRUI_CURRENT_REVISION ?: '',
        thinkwifi_BRANCH: THINKWIFI_MANIFEST_BRANCH_OR_REVISION ?: '',
        thinkwifi_COMMIT_ID: THINKWIFI_CURRENT_REVISION ?: '',
        abchmiclientapk_BRANCH: ABCHMICLIENTAPK_MANIFEST_BRANCH_OR_REVISION ?: '',
        abchmiclientapk_COMMIT_ID: ABCHMICLIENTAPK_CURRENT_REVISION ?: '',
        thinkcarplay_BRANCH: THINKCARPLAY_MANIFEST_BRANCH_OR_REVISION ?: '',
        thinkcarplay_COMMIT_ID: THINKCARPLAY_CURRENT_REVISION ?: '',
        RC_NAME: RC_NAME ?: '',
        AOSP_TAG_COMMIT_ID_USER: "${QWM2290_ANDROID14_0_R03_R002_5_2W_CURRENT_REVISION}",
        AOSP_TAG_COMMIT_ID_DEBUG: "${QWM2290_ANDROID14_0_R03_R002_5_2W_CURRENT_REVISION}",
        AOSP_SW_VERSION: "${RC_NAME}",
        EMAIL_TO: EMAIL_TO ?: '',
        BUILD_NUMBER: BUILD_NUMBER,
        BUILD_URL: BUILD_URL,
        JOB_BASE_NAME: JOB_BASE_NAME,
        JOB_NAME: JOB_NAME,
        PROJECT_NAME: PROJECT_NAME ?: '',
        CONTAINER_NAME: CONTAINER_NAME,
        SLAVE_NODE_IP: SLAVE_NODE_IP ?: '',
        AZURE_CONTAINER_DIR: AZURE_CONTAINER_DIR ?: '',
        BUILD_DISPLAY_NAME: SOFTWARE_RELEASE_BUILD_DIR ?: '',
        PROJECT_NUMBER: PROJECT_NUMBER ?: '',
        BUILD_VARIANT: BUILD_VARIANT ?: '',
    ]
}

/**
 * Generate change logs for all projects
 */
def generateChangeLogsForAllProjects() {
    def projectChangeLogConfigs = [
        "AOSP": [dir: BASE_AOSP_DIR, branch: params.BASE_AOSP_TAG_OR_BRANCH_NAME, name: "QWM2290_Android14.0_R03_r002.5_2W"],
        "btservicemanagerapk": [dir: BTConnectionManager_DIR, branch: params.BTConnectionManager, name: "btservicemanagerapk"],
        "navigationmanagerapk": [dir: NavigationManager_DIR, branch: params.NavigationManager, name: "navigationmanagerapk"],
        "profilemanagerapk": [dir: ProfileManager_DIR, branch: params.ProfileManager, name: "profilemanagerapk"],
        "systemhealthmonitorapk": [dir: SystemHealthMonitor_DIR, branch: params.SystemHealthMonitor, name: "systemhealthmonitorapk"],
        "abcaudioservicemanagerapk": [dir: AudioServiceManager_DIR, branch: params.AudioServiceManager, name: "abcaudioservicemanagerapk"],
        "otamanagerapk": [dir: OtaManager_DIR, branch: params.OtaManager, name: "otamanagerapk"],
        "codpapiapk": [dir: CODPAPIManager_DIR, branch: params.CodPapiManager, name: "codpapiapk"],
        "wifimanagerapk": [dir: WifiManager_DIR, branch: params.WifiManager, name: "wifimanagerapk"],
        "mediaservicemanagerapk": [dir: MediaServiceManager_DIR, branch: params.MediaServiceManager, name: "mediaservicemanagerapk"],
        "twowheelerserviceapk": [dir: TwoWheelerService_DIR, branch: params.TwoWheelerService, name: "twowheelerserviceapk"],
        "telephonymanagerapk": [dir: TelephonyManager_DIR, branch: params.TelephonyManager, name: "telephonymanagerapk"],
        "aaprojectionapk": [dir: Projection_DIR, branch: params.AAProjection, name: "aaprojectionapk"],
        "thinkseedrui": [dir: ThinkSeedRui_DIR, branch: params.ThinkSeedRui, name: "thinkseedrui"],
        "thinkwifi": [dir: ThinkWifi_DIR, branch: params.ThinkWifi, name: "thinkwifi"],
        "abchmiclientapk": [dir: ABCHMIClientapk_DIR, branch: params.ABCHMIClientAPK, name: "abchmiclientapk"],
        "thinkcarplay": [dir: ThinkCarPlay_DIR, branch: params.ThinkCarPlay, name: "thinkcarplay"],
    ]
    
    def variables = [:]
    def elmIDs = []
    
    projectChangeLogConfigs.each { refTypePrefix, config ->
        try {
            def changeLogResult
            if (params.AOSP_USER_BUILD) {
                changeLogResult = aospChangeLogForDailyBuilds("${aosp_user_container}", config.dir, TODAY_DATE, YESTERDAY_DATE)
            } else if (params.AOSP_DEBUG_BUILD) {
                changeLogResult = aospChangeLogForDailyBuilds("${aosp_debug_container}", config.dir, TODAY_DATE, YESTERDAY_DATE)
            } else {
                changeLogResult = aospChangeLogForDailyBuilds("${aosp_debug_container}", config.dir, TODAY_DATE, YESTERDAY_DATE)
            }
            //def changeLogResult = aospChangeLogForDailyBuilds("${aosp_user_container}", config.dir, TODAY_DATE, YESTERDAY_DATE)
            variables["${refTypePrefix}_PREVIOUS_RELEASED_TAG_NAME"] = changeLogResult[1]
            variables["${refTypePrefix}_tagchanges_stat"] = changeLogResult[2]
            variables["${refTypePrefix}_COMMIT_COUNT"] = changeLogResult[3]
            elmIDs << changeLogResult[4]
        } catch (Exception e) {
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
        def base_url = "https://abcmelmsrvr.abcmotor.com/ccm/web/projects/.OS%20Platform#action=com.ibm.team.workitem.viewWorkItem&id="
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
    
    def htmlTemplate = readFile '/data/tool/AOSP_A12/A12_DEV_AOSP_success_email_template_new.html'
    htmlTemplate = replacePlaceholders(htmlTemplate, variables)
    htmlTemplate = replacePlaceholders(htmlTemplate, buildDetails)
    
    return htmlTemplate
}

/**
 * Send email notification
 */
def sendEmailNotification(htmlContent) {
    dir("${WORKSPACE}") {
            writeFile file: "${PROJECT_NAME}_${AOSP_SW_VERSION}_${DATE}_#${BUILD_NUMBER}.html", text: htmlContent
            sh """
                wkhtmltopdf "${WORKSPACE}/${PROJECT_NAME}_${AOSP_SW_VERSION}_${DATE}_#${BUILD_NUMBER}.html" "${WORKSPACE}/${PROJECT_NAME}_${AOSP_SW_VERSION}_${DATE}_#${BUILD_NUMBER}.pdf" || true
            """
        }

    archiveArtifacts artifacts: "**/*.pdf", followSymlinks: false, onlyIfSuccessful: true

    sh """
        pwd
        n=0; until [ \$n -ge 5 ]; do sudo azcopy copy "${WORKSPACE}/${PROJECT_NAME}_${AOSP_SW_VERSION}_${DATE}_#${BUILD_NUMBER}.pdf" 'https://${AZURE_STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AZURE_SOFTWARE_RELEASE_BUILD_DIR}/?${SAS_KEY_INT}' --recursive && break; n=\$((n+1)); sleep 5; done || true
        aws s3 cp "${WORKSPACE}/${PROJECT_NAME}_${AOSP_SW_VERSION}_${DATE}_#${BUILD_NUMBER}.pdf" s3://abcm-rnd-s3-uat01/${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${AWS_SOFTWARE_RELEASE_BUILD_DIR}/ 1>/dev/null
    """
    
    emailext \
        subject: """[${PROJECT_NAME}-DAILY BUILD] AOSP SW Version: ${AOSP_SW_VERSION}  --> #${BUILD_NUMBER}""",
        body: """ ${htmlContent} """,
        to: """${EMAIL_TO}""",
        attachmentsPattern: """**/*.pdf"""
}

/**
 * Update Django database if enabled
 */
def updateDjangoDatabase(buildDetails, databaseElmId) {
    def djangoDatabaseUpdate = (DJANGO_DATABASE_UPDATE != null && DJANGO_DATABASE_UPDATE != '') ? DJANGO_DATABASE_UPDATE.toBoolean() : false
    // Run Django management command with Jenkins parameters
    if (djangoDatabaseUpdate.toBoolean()) {
        try {
            echo "Starting Django database update on remote server..."

            // SSH command with error handling
            sh """
            ssh swcocuser@10.121.4.232 'cd /home/swcocuser/django_web_app/django && . ../venv/bin/activate && python manage.py dailybuilds_cluster_create_instance \
                --project="${PROJECT_NUMBER}" \
                --aosp_tag_name="${AOSP_TAG}" \
                --aosp_sw_version="${RC_NAME}" \
                --aosp_commit_id="${QWM2290_ANDROID14_0_R03_R002_5_2W_CURRENT_REVISION}" \
                --cluster_app_tag_name="" \
                --cluster_app_sw_version="" \
                --cluster_commit_id="" \
                --ota_client_app_tag_name="" \
                --ota_client_commit_id="" \
                --pki_app_tag_name="" \
                --pki_commit_id="" \
                --environment="" \
                --binary_path="${CONTAINER_NAME}/${AZURE_CONTAINER_DIR}/${PROJECT_NAME}/${SOFTWARE_RELEASE_BUILD_DIR}" \
                --build_url="${BUILD_URL}" \
                --build_id="${BUILD_DISPLAY_NAME}" \
                --build_variant="${BUILD_VARIANT}" \
                --bugs="${DATABASE_ELM_ID}" \
                --release_date="${DATE}" \
                --package_name="${RC_NAME}"
                '
            """
            echo "Django database update completed successfully."
        } catch (Exception e) {
            error "Failed to update Django database: ${e.message}"
        }
    } else {
        echo "No Need to Update Django DATABASE"
    }
}

