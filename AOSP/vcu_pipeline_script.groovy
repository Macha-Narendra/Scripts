pipeline {
    agent {
        label "${SLAVE_NODE_IP}"
    }
    
    
    /*parameters {
        string(name: 'TAG_NAME', defaultValue: "", description: 'Enter a TAG NAME')
    }*/
    

    environment {
        // Set environment variables if needed
        ANSI_RESET = "\u001B[0m"
        ANSI_RED = "\u001B[31m"
        ANSI_BLUE = "\033[34m"
        ANSI_PURPLE = "\033[35m"
        ANSI_GREEN = "\033[32m"
        FAILED_STAGE = ""
        CRC_VALUE = "None"
        //PATH = "/opt/S32DS/build_tools/gcc_v6.3/gcc-6.3-arm32-eabi/bin:$PATH"
        PATH = "/opt/S32DS_3.4/S32DS/build_tools/gcc_v6.3/gcc-6.3-arm32-eabi/bin:$PATH"
        //BITBUCKET_USERNAME = "narendra.babu"
        //BITBUCKET_APP_PASSWORD = credentials("narendra_id")
        EMAIL_TO_INT = "sw-release@example.com"
        OUT_DIR = "output"
        FIRMWARE_DIR = "/data/firmware"
        PROJECT_NAME = ""
        CONTAINER_NAME = "integration"
        CONTAINER_DIR = "internal"
        AZURE_STORAGE_ACCOUNT = "zzzzz"
        SAS_KEY_DEV = "xxxxxx"
        SAS_KEY_INT = "yyyy"
    }

    options {   
        timestamps()
        //buildDiscarder(logRotator(numToKeepStr: '10'))
        ansiColor('xterm')
    }

    stages {
        stage('CLEAN_UP_WORKSPACE') {
            when {
                expression { params.CLEAN_UP_WORKSPACE }
            }
            steps {
                script {
                    try {
                        // Example: Cleanup
                        echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"

                        // Get the current directory using the pwd() function
                        def currentDir = pwd()

                        // Getting DATE and TIME info
                        def currentDate = sh(script: "date +'%Y-%m-%d %H:%M'", returnStdout: true).trim()
                        def todayDate = sh(script: "date +'%Y-%m-%d'", returnStdout: true).trim()
                        def yesterdayDate = sh(script: 'date -d "yesterday" +"%Y-%m-%d"', returnStdout: true).trim()
                        def currentDay = sh(script: 'date +%d', returnStdout: true).trim()
                        def currentMonth = sh(script: 'date +%m', returnStdout: true).trim()

                        TODAY_DATE = "${currentDate}"
                        YESTERDAY_DATE = "${yesterdayDate}"
                        DATE = "${todayDate}"

                        cleanWs()
                    } catch (Exception e) {
                        echo "${ANSI_RED}FAILED: JOB is failed at ${STAGE_NAME} stage with an error as : ${e.message}${ANSI_RESET}"
                        currentBuild.result = 'FAILURE'
                        FAILED_STAGE = "${STAGE_NAME}"
                        error("ERROR: at ${STAGE_NAME}", e.message)
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
                        echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"

                        if (TAG_NAME == "") {
                            // Checkout the code from Bitbucket
                            checkout([$class: 'GitSCM', branches: [[name: "master"]], userRemoteConfigs: [[url: "${PROJECT_URL}", credentialsId: 'narendra_id']]])
                            //TAG_NAME = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', returnStdout: true).trim()
                            TAG_NAME = sh(script: 'git tag -l --sort=-creatordate | sed -n 1p', returnStdout: true).trim()
                            echo "${ANSI_GREEN}TAG_NAME: ${TAG_NAME}${ANSI_RESET}"
                            cleanWs()
                        } 
                        // Checkout the code from Bitbucket
                        echo "${ANSI_GREEN}TAG_NAME: ${TAG_NAME}${ANSI_RESET}"
                        checkout([$class: 'GitSCM', branches: [[name: "${TAG_NAME}"]], userRemoteConfigs: [[url: "${PROJECT_URL}", credentialsId: 'narendra_id']]])
                        
                        // Using Git credentials to checkout the code
                        //git branch: "cdeac0607f2c51ee4aaab4d937297488b98b4ee8", url: 'ssh://git@10.121.4.69:7999/swrndrtm/test.git'

                        // Capture Git-related environment variables
                        GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                        GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        GIT_AUTHOR = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
                        GIT_AUTHOR_EMAIL = sh(script: 'git log -1 --pretty=%ae', returnStdout: true).trim()
                        GIT_COMMITTER = sh(script: 'git log -1 --pretty=%cn', returnStdout: true).trim()
                        GIT_COMMITTER_EMAIL = sh(script: 'git log -1 --pretty=%ce', returnStdout: true).trim()
                        echo "${ANSI_BLUE}GIT_COMMIT: ${GIT_COMMIT}${ANSI_RESET}"
                        echo "${ANSI_BLUE}GIT_COMMIT_SHORT: ${GIT_COMMIT_SHORT}${ANSI_RESET}"
                        echo "${ANSI_BLUE}GIT_AUTHOR: ${GIT_AUTHOR}${ANSI_RESET}"
                        echo "${ANSI_BLUE}GIT_AUTHOR_EMAIL: ${GIT_AUTHOR_EMAIL}${ANSI_RESET}"
                        echo "${ANSI_BLUE}GIT_COMMITTER: ${GIT_COMMITTER}${ANSI_RESET}"
                        echo "${ANSI_BLUE}GIT_COMMITTER_EMAIL: ${GIT_COMMITTER_EMAIL}${ANSI_RESET}"

                        // Fetching info for PREVIOUS_RELEASED_TAG_NAME 
                        if (PREVIOUS_RELEASED_TAG_NAME == "") {
                            echo "PREVIOUS_RELEASED_TAG_NAME is Empty."
                            PREVIOUS_RELEASED_TAG_NAME = sh(script: "git tag --sort=-creatordate --merged ${TAG_NAME}^ | sed -n 1p", returnStdout: true).trim()
                            echo "${ANSI_BLUE}PREVIOUS_RELEASED_TAG_NAME: ${PREVIOUS_RELEASED_TAG_NAME}${ANSI_RESET}"
                        }
                        echo "${ANSI_BLUE}PREVIOUS_RELEASED_TAG_NAME: ${PREVIOUS_RELEASED_TAG_NAME}${ANSI_RESET}"

                        def SW_VERSION_FILE = new File("Project_ASW_Settings/System_HexInfos_Project.h")
                        
                        // Application version fetching
                        Software_Version_Major = softwareVersionFetching(SW_VERSION_FILE, "#define C_Sofware_Variant_Major_UB")
                        Software_Version_Minor = softwareVersionFetching(SW_VERSION_FILE, "#define C_Sofware_Variant_Minor_UB")
                        Software_Version_Micro = softwareVersionFetching(SW_VERSION_FILE, "#define C_Sofware_Variant_Micro_UB")
                        Software_Version_ECU_Identification = softwareVersionFetching(SW_VERSION_FILE, "#define C_ECU_Identification_UB")
                        Application_Software_Version = softwareVersionFetching(SW_VERSION_FILE, "#define C_Application_Software_UB")
                        Software_Version_Category = softwareVersionFetching(SW_VERSION_FILE, "#define C_Software_Category_UB")
                        
                        // Bootloader version fetching
                        BL_Software_Version_Major = softwareVersionFetching(SW_VERSION_FILE, "#define C_BL_Sofware_Variant_Major_UB")
                        BL_Software_Version_Minor = softwareVersionFetching(SW_VERSION_FILE, "#define C_BL_Sofware_Variant_Minor_UB")
                        BL_Software_Version_Micro = softwareVersionFetching(SW_VERSION_FILE, "#define C_BL_Sofware_Variant_Micro_UB")
                        BL_Software_Version_ECU_Identification = softwareVersionFetching(SW_VERSION_FILE, "#define C_ECU_Identification_UB")
                        BL_Software_Version = softwareVersionFetching(SW_VERSION_FILE, "#define C_BootLoader_Software_UB")
                        BL_Software_Version_Category = softwareVersionFetching(SW_VERSION_FILE, "#define C_BL_Software_Category_UB")

                        APP_SW_VERSION = sh(script:"echo '${Software_Version_Major}.${Software_Version_Minor}.${Software_Version_Micro}.${Software_Version_Category}'", returnStdout: true).trim()

                        def appSWName = APP_SW_VERSION ?: ''
                        if (appSWName == "") {
                            //Writing DATE if APP_SW_VERSION value is empty.
                            APP_SW_VERSION = sh(script:"""date +'%Y.%m.%d.%H'""", returnStdout: true).trim()
                        }
                        
                        BL_SW_VERSION = sh(script:"echo '${BL_Software_Version_Major}.${BL_Software_Version_Minor}.${BL_Software_Version_Micro}.${BL_Software_Version_Category}'", returnStdout: true).trim()
                        echo "${ANSI_RED}APP_SW_VERSION: ${APP_SW_VERSION}${ANSI_RESET}"
                        echo "${ANSI_BLUE}BL_SW_VERSION: ${BL_SW_VERSION}${ANSI_RESET}"

                        BATTERY_PACK_VERSION = softwareVersionFetching(SW_VERSION_FILE, "#define C_Configured_No_Of_Packs_UB")

                        // Fetching BATTERY PACK Details
                        /*try {
                            def BATTERY_PACK_FILE = new File("src/FeatureConfig.h")
                            BATTERY_PACK_VERSION = sh(script:"""cat ${BATTERY_PACK_FILE} | grep "#define NUMOFBATTPACKS" |awk -F' ' '{print \$3}' |tr -d ' ' """, returnStdout: true).trim()
                        } catch (Exception e) {
                            println "Error got whle fetching BATTER PACK VERSION: ${e}"
                            BATTERY_PACK_VERSION = "-"
                        }*/

                        echo "${ANSI_BLUE}BATTERY_PACK_VERSION: ${BATTERY_PACK_VERSION}${ANSI_RESET}"

                        /*def projectName = env.PROJECT_NAME ?: ''
                        if (!(projectName)) {
                            // OVERWRITING PROJECT_NAME VARIABLE and FECTCHING IT FROM SOURCE FILE build/makefile
                            PROJECT_NAME = sh(script:"""cat build/makefile | grep "VARIANT :=" | awk -F' ' '{print \$3}'""", returnStdout: true).trim()
                            projectName = "${PROJECT_NAME}"
                        }
                        if (projectName == "") {
                            // OVERWRITING PROJECT_NAME VARIABLE and FECTCHING IT FROM SOURCE FILE build/project.mk
                            PROJECT_NAME = sh(script:"""cat build/project.mk | grep "VARIANT :=" | awk -F' ' '{print \$3}'""", returnStdout: true).trim()
                        }*/

                        projectCode_1 = softwareVersionFetching(SW_VERSION_FILE, "#define C_ProjectCode1_UB")
                        projectCode_2 = softwareVersionFetching(SW_VERSION_FILE, "#define C_ProjectCode2_UB")
                        projectCode_3 = softwareVersionFetching(SW_VERSION_FILE, "#define C_ProjectCode3_UB")
                        projectCode_4 = softwareVersionFetching(SW_VERSION_FILE, "#define C_ProjectCode4_UB")

                        def projectCodes = ["${projectCode_1}", "${projectCode_2}", "${projectCode_3}", "${projectCode_4}"]
                        def asciiString = convertHexToAscii(projectCodes)

                        PROJECT_NAME = "${asciiString}"
                        echo "${ANSI_BLUE}PROJECT_NAME: ${PROJECT_NAME}${ANSI_RESET}"

                        // Meta Data That needs to write in the first(1st) line on the final hex binary file.
                        line_1_VehicleCategory_Engintype = softwareVersionFetching(SW_VERSION_FILE, "#define C_VehicleCategory_Engintype_UB").trim()
                        line_1_Software_PartNumber1 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Software_PartNumber1_UB").trim()
                        line_1_Software_PartNumber2 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Software_PartNumber2_UB").trim()
                        line_1_Software_PartNumber3 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Software_PartNumber3_UB").trim()
                        line_1_Software_PartNumber4 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Software_PartNumber4_UB").trim()
                        line_1_Software_PartNumber5 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Software_PartNumber5_UB").trim()
                        line_1_VehicleIdentification1 = softwareVersionFetching(SW_VERSION_FILE, "#define C_VehicleIdentification1_UB").trim()
                        line_1_VehicleIdentification2 = softwareVersionFetching(SW_VERSION_FILE, "#define C_VehicleIdentification2_UB").trim()
                        line_1_VehicleIdentification3 = softwareVersionFetching(SW_VERSION_FILE, "#define C_VehicleIdentification3_UB").trim()
                        line_1_Vehicle_Model_Type = softwareVersionFetching(SW_VERSION_FILE, "#define C_Vehicle_Model_Type_UB").trim()
                        line_1_ECU_Identification = softwareVersionFetching(SW_VERSION_FILE, "#define C_ECU_Identification_UB").trim()
                        line_1_Sofware_Variant_Major = softwareVersionFetching(SW_VERSION_FILE, "#define C_Sofware_Variant_Major_UB").trim()
                        line_1_Sofware_Variant_Minor = softwareVersionFetching(SW_VERSION_FILE, "#define C_Sofware_Variant_Minor_UB").trim()
                        line_1_Sofware_Variant_Micro = softwareVersionFetching(SW_VERSION_FILE, "#define C_Sofware_Variant_Micro_UB").trim()
                        line_1_Software_Category = softwareVersionFetching(SW_VERSION_FILE, "#define C_Software_Category_UB").trim()
                        line_1_Programming_Year = softwareVersionFetching(SW_VERSION_FILE, "#define C_Programming_Year_UB").trim()
                        line_1_Programming_Month = softwareVersionFetching(SW_VERSION_FILE, "#define C_Programming_Month_UB").trim()
                        line_1_C_Programming_Date = softwareVersionFetching(SW_VERSION_FILE, "#define C_Programming_Date_UB").trim()

                        // Meta Data That needs to write in the second(2nd) line on the final hex binary file.
                        line_2_SAP_Hardware_PartNumber1 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Hardware_PartNumber1_UB").trim()
                        line_2_SAP_Hardware_PartNumber2 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Hardware_PartNumber2_UB").trim()
                        line_2_SAP_Hardware_PartNumber3 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Hardware_PartNumber3_UB").trim()
                        line_2_SAP_Hardware_PartNumber4 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Hardware_PartNumber4_UB").trim()
                        line_2_SAP_Hardware_PartNumber5 = softwareVersionFetching(SW_VERSION_FILE, "#define C_SAP_Hardware_PartNumber5_UB").trim()
                        line_2_VehicleIdentification1 = softwareVersionFetching(SW_VERSION_FILE, "#define C_VehicleIdentification1_UB").trim()
                        line_2_VehicleIdentification2 = softwareVersionFetching(SW_VERSION_FILE, "#define C_VehicleIdentification2_UB").trim()
                        line_2_VehicleIdentification3 = softwareVersionFetching(SW_VERSION_FILE, "#define C_VehicleIdentification3_UB").trim()
                        line_2_ECU_Identification = softwareVersionFetching(SW_VERSION_FILE, "#define C_ECU_Identification_UB").trim()
                        line_2_ECU_Variant_Major = softwareVersionFetching(SW_VERSION_FILE, "#define C_ECU_Variant_Major_UB").trim()
                        line_2_ECU_Variant_Minor = softwareVersionFetching(SW_VERSION_FILE, "#define C_ECU_Variant_Minor_UB").trim()
                        line_2_ECU_Variant_Micro = softwareVersionFetching(SW_VERSION_FILE, "#define C_ECU_Variant_Micro_UB").trim()
                        line_2_BOM_Identification = softwareVersionFetching(SW_VERSION_FILE, "#define C_BOM_Identification_UB").trim()

                        def line_projectCodes = ["${line_1_Software_Category}"]
                        def line_1_Software_Category_asciiString = convertHexToAscii(line_projectCodes)

                        echo "line_1_Software_Category: ${line_1_Software_Category}"

                        def softwareCategoryMap = [
                            "0D": "04", // Production
                            "0C": "03", // Running Change
                            "0B": "02", // Beta
                            "0A": "01" // Alpha
                        ]

                        def line_1_Software_Category_lookup = softwareCategoryMap.get(line_1_Software_Category)
                        // Print the result
                        println "Software Category Lookup Value: ${line_1_Software_Category_lookup}"

                        Software_Part_Number = "${line_1_Software_PartNumber1}${line_1_Software_PartNumber2}${line_1_Software_PartNumber3}${line_1_Software_PartNumber4}${line_1_Software_PartNumber5}"

                        line_1 = sh(script:"""
                                 echo -n "${line_1_VehicleCategory_Engintype}${line_1_Software_PartNumber1}
                                 ${line_1_Software_PartNumber2}${line_1_Software_PartNumber3}${line_1_Software_PartNumber4}
                                 ${line_1_Software_PartNumber5}${projectCode_2}${projectCode_3}${projectCode_4}${line_1_Vehicle_Model_Type}${line_1_ECU_Identification}
                                 ${line_1_Sofware_Variant_Major}${line_1_Sofware_Variant_Minor}${line_1_Sofware_Variant_Micro}
                                 ${line_1_Software_Category_lookup}${line_1_Programming_Year}${line_1_Programming_Month}
                                 ${line_1_C_Programming_Date}"|tr -d '[:space:]' | tr -d 'U //' """, returnStdout: true).trim()

                        line_2 = sh(script:"""
                                 echo -n "${line_2_SAP_Hardware_PartNumber1}${line_2_SAP_Hardware_PartNumber2}
                                 ${line_2_SAP_Hardware_PartNumber3}${line_2_SAP_Hardware_PartNumber4}${line_2_SAP_Hardware_PartNumber5}
                                 ${projectCode_2}${projectCode_3}${projectCode_4}
                                 ${line_2_ECU_Identification}${line_2_ECU_Variant_Major}${line_2_ECU_Variant_Minor}
                                 ${line_2_ECU_Variant_Micro}${line_2_BOM_Identification}"|tr -d '[:space:]' |tr -d 'U //' """, returnStdout: true).trim()

                        META_INFO_LINE_1 = "${line_1}"
                        META_INFO_LINE_2 = "${line_2}"
                        echo "${ANSI_GREEN}META_INFO_LINE_1: ${META_INFO_LINE_1}${ANSI_RESET}"
                        echo "${ANSI_RED}META_INFO_LINE_2: ${META_INFO_LINE_1}${ANSI_RESET}"

                        //Webhook change logs
                        def changeLogSets = currentBuild.changeSets
                        def ListOfAuthors = []
                        def ListOfCommits = []
                        echo "changeLogSets: ${changeLogSets}"
                        for (int i = 0; i < changeLogSets.size(); i++) {
                            def entries = changeLogSets[i].items
                            for (int j = 0; j < entries.length; j++) {
                                def entry = entries[j]
                                echo "entry: ${entry}"
                                echo "Author: ${entry.author.fullName}, Message: ${entry.msg}"
                                ListOfAuthors.add("${entry.author.fullName}")
                                echo "Affected paths: ${entry.affectedPaths.join(', ')}"
                                echo "Commit ID: ${entry.commitId}"
                                ListOfCommits.add("${entry.commitId.trim()}")                            
                            }
                        }
                        echo "ListOfAuthors: ${ListOfAuthors}"
                        echo "ListOfCommits: ${ListOfCommits}"
                        def StringOfListOfAuthors = ListOfAuthors.toString().replaceAll("\\[|\\]", "")
                        echo "StringOfListOfAuthors: ${StringOfListOfAuthors}"
                        FinalAuthors=StringOfListOfAuthors
                        FinalListOfCommits = ListOfCommits
                        echo "${ANSI_BLUE}FinalAuthors: ${FinalAuthors}${ANSI_RESET}"
                        echo "${ANSI_BLUE}FinalListOfCommits: ${FinalListOfCommits}${ANSI_RESET}"
                    } catch (Exception e) {
                        echo "FAILED: JOB is failed at ${STAGE_NAME} stage with an error as : ${e.message}"
                        //echo "Failed to checkout code: ${e}"
                        currentBuild.result = 'FAILURE'
                        FAILED_STAGE = "${STAGE_NAME}"
                        error("ERROR: at ${STAGE_NAME} ", e.message)
                    }
                }
            }
        }

        stage('CODE_QUALITY') {
            when {
                expression { params.CODE_QUALITY }
            }
            steps {
                script {
                    echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"
                    /*sh """
                     cp /data/tool/LDRA_Report_vcu_2w_u546_application_sw.txt ${WORKSPACE}
                    """*/
                    sleep time: 2, unit: 'SECONDS' // Sleep for 2 seconds
                }
            }
        }

        stage('SECURITY_SCANNER') {
            when {
                expression { params.SECURITY_SCANNER }
            }
            steps {
                script {
                    echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"
                }
            }
        }

        stage('BUILD') {
            when {
                expression { params.BUILD }
            }
            steps {
                script {
                    try {
                        echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"
                        sh """
                        # RUNNING BUILD.SH FILE to GENERATE BINARIES
                        chmod +x *.sh
                        bash build.sh
                        """
                    } catch (Exception e) {
                        echo "${ANSI_RED}FAILED: JOB is failed at ${STAGE_NAME} stage with an error as : ${e.message}${ANSI_RESET}"
                        currentBuild.result = 'FAILURE'
                        FAILED_STAGE = "${STAGE_NAME}"
                        error("ERROR: at ${STAGE_NAME} stage as ", e.message)
                    }
                }
            }
        }

        stage('INTEGRATION_TEST') {
            when {
                expression { params.INTEGRATION_TEST }
            }
            steps {
                script {
                    try {
                        // Example: Run tests
                        echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"
                    } catch (Exception e) {
                        echo "${ANSI_RED}FAILED: JOB is failed at ${STAGE_NAME} stage with an error as : ${e.message}${ANSI_RESET}"
                        currentBuild.result = 'FAILURE'
                        FAILED_STAGE = "${STAGE_NAME}"
                        error("ERROR: at ${STAGE_NAME} stage as ", e.message)
                    }
                }
            }
        }

        stage('PACKAGING_ARTIFACTS') {
            when {
                expression { params.PACKAGING_ARTIFACTS }
            }
            steps {
                script {
                    try {
                        // Creating Artifacts
                        echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"
                        sh """
                        # Moving SW_VERSION direcotry to SW_VERSION_OLD in FIRMWARE DIR to copy FRESH BINIRAIES.
                        if [ -d ${FIRMWARE_DIR}/${PROJECT_NAME}/${APP_SW_VERSION} ]
                        then
                            mv ${FIRMWARE_DIR}/${PROJECT_NAME}/${APP_SW_VERSION} ${FIRMWARE_DIR}/${PROJECT_NAME}/${APP_SW_VERSION}_#${BUILD_NUMBER}_OLD
                        fi
                        mkdir -p ${FIRMWARE_DIR}/${PROJECT_NAME}/${APP_SW_VERSION}_#${BUILD_NUMBER}
                        """

                        //Modifying CRC value for the .hex file
                        CRC_VALUE = sh(script: "cd ${OUT_DIR}/${PROJECT_NAME} && python3 /data/tool/CRC_check.py *.hex", returnStdout: true).trim()
                        CRC_VALUE = sh(script: "echo 0x${CRC_VALUE}", returnStdout: true).trim()
                        
                        // Calculating BINARY SIXE OF ".hex" file
                        BIN_SIZE = sh(script: """cd ${OUT_DIR}/${PROJECT_NAME} && arm-none-eabi-size *.hex| awk -F' ' '{print \$4}' | sed -n 2p """, returnStdout: true).trim()
                        BINARY_SIZE = sh(script: """echo "\$[${BIN_SIZE}/1024]" """, returnStdout: true).trim()
                        
                        // Execute md5sum command to calculate the MD5 checksum
                        MD5SUM = sh(script: """cd ${OUT_DIR}/${PROJECT_NAME} && md5sum *.hex|awk -F' ' '{print \$1}'""", returnStdout: true).trim()
                        
                        echo "${ANSI_PURPLE}CRC_VALUE: ${CRC_VALUE}${ANSI_RESET}"
                        echo "${ANSI_PURPLE}BINARY_SIZE: ${BINARY_SIZE} kB${ANSI_RESET}"
                        echo "${ANSI_PURPLE}MD5SUM: ${MD5SUM}${ANSI_RESET}"
                        
                        sh """
                        #COPYING LATEST BINARIES TO FIRMWARE DIRECTORY
                        cd ${OUT_DIR}/${PROJECT_NAME}
                        mv *.hex ${PROJECT_NAME}_${Software_Part_Number}_${APP_SW_VERSION}_${CRC_VALUE}.hex
                        cp ${PROJECT_NAME}_${Software_Part_Number}_${APP_SW_VERSION}_${CRC_VALUE}.hex ${PROJECT_NAME}_${Software_Part_Number}_${APP_SW_VERSION}_${CRC_VALUE}_META.hex
                        mv *.elf ${PROJECT_NAME}_${Software_Part_Number}_${APP_SW_VERSION}_${CRC_VALUE}.elf
                        cp *.hex *.elf ${FIRMWARE_DIR}/${PROJECT_NAME}/${APP_SW_VERSION}_#${BUILD_NUMBER}
                        """

                        sh """
                            # Writing two meta lines in the hex file
                            cd ${OUT_DIR}/${PROJECT_NAME}
                            sed -i "1i ${META_INFO_LINE_1}" ${PROJECT_NAME}_${Software_Part_Number}_${APP_SW_VERSION}_${CRC_VALUE}_META.hex
                            sed -i "2i ${META_INFO_LINE_2}" ${PROJECT_NAME}_${Software_Part_Number}_${APP_SW_VERSION}_${CRC_VALUE}_META.hex

                            # Reading META HEX file after inserting META INFO
                            cat ${PROJECT_NAME}_${Software_Part_Number}_${APP_SW_VERSION}_${CRC_VALUE}_META.hex | head -n 5
                        """
                    } catch (Exception e) {
                        echo "${ANSI_RED}FAILED: JOB is failed at ${STAGE_NAME} stage with an error as : ${e.message}${ANSI_RESET}"
                        currentBuild.result = 'FAILURE'
                        FAILED_STAGE = "${STAGE_NAME}"
                        error("ERROR: at ${STAGE_NAME} stage as ", e.message)
                    }
                }
            }
        }

        stage('DEPLOY_TO_STAGING') {
            when {
                expression { params.DEPLOY_TO_STAGING }
            }
            steps {
                script {
                    try {
                        // Example: Deploy to production
                        echo "${ANSI_PURPLE}${env.STAGE_NAME}${ANSI_RESET}"
                        /*def azcopyPath = sh(script: 'which azcopy', returnStatus: true).exitCode
                        if (azcopyPath == 0) {
                            echo "azcopy is installed"
                        } else {
                            echo "azcopy is not installed"
                            curl -o azcopy_linux.tar.gz https://aka.ms/downloadazcopy-v10-linux && tar -xf azcopy_linux.tar.gz --strip-components=1 && sudo mv azcopy /usr/local/bin/ && rm azcopy_linux.tar.gz
                        }*/
                        sh """
                        # UPLOADING SW_VERSION DIRECOTRY to AZURE
                        cd ${FIRMWARE_DIR}/${PROJECT_NAME}
                        azcopy copy "${APP_SW_VERSION}_#${BUILD_NUMBER}" "https://${AZURE_STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${CONTAINER_DIR}/${PROJECT_NAME}/?${SAS_KEY_INT}" --recursive=true
                        """
                    } catch (Exception e) {
                        echo "${ANSI_RED}FAILED: JOB is failed at ${STAGE_NAME} stage with an error as : ${e.message}${ANSI_RESET}"
                        currentBuild.result = 'FAILURE'
                        FAILED_STAGE = "${STAGE_NAME}"
                        error("ERROR: at ${STAGE_NAME} stage as ", e.message)
                    }
                }
            }
        }
    }

    post {
        always {
            // Clean up resources or perform actions that need to be done in all cases
            echo "Pipeline succeeded! Sending notifications..."
        }
        success {
            // Actions to be performed when the pipeline is successful
            echo "Pipeline succeeded! Sending notifications..."
            script {
                currentBuild.displayName = "${APP_SW_VERSION}_#${BUILD_NUMBER}"
                currentBuild.description = """
                TAG_NAME: <b>${TAG_NAME}</b> <br>
                COMMIT_ID: <b>${GIT_COMMIT_SHORT}</b>
                """
            }
            script {

                echo "Diff between ${TAG_NAME} and ${PREVIOUS_RELEASED_TAG_NAME}:"

                // Execute git log --stat command
                def gitDiff = sh(script: "git diff ${PREVIOUS_RELEASED_TAG_NAME}..${TAG_NAME} --numstat", returnStdout: true).trim()
                def logStatOutput = sh(script: "git log ${PREVIOUS_RELEASED_TAG_NAME}..${TAG_NAME} --numstat",returnStdout: true).trim()
                def logStatOutput_short = sh(script: "git diff ${PREVIOUS_RELEASED_TAG_NAME}..${TAG_NAME} --shortstat",returnStdout: true).trim()

                // Parse and format the output
                def (tagchanges_stat, elmIDs) = formatGitLogOutput(logStatOutput.split("\\n"))
                def diffchanges = calculateAndSendDiff(gitDiff, logStatOutput_short)

                echo "elmIDs: ${elmIDs}"
                // Process ELM_ID: flatten, extract unique, sort, and handle empty case
                def ELM_ID = elmIDs.flatten().unique().sort()
                if (ELM_ID.isEmpty()) {
                    ELM_ID = "-" // Set to empty string if no IDs
                    DATABASE_ELM_ID = ""
                } else {
                    // Base URL for links
                    def base_url = "elm url"

                    DATABASE_ELM_ID = ELM_ID.join(', ') // Convert to a comma-separated string
                    // Convert each ID into a hyperlink
                    ELM_ID = ELM_ID.collect { id ->
                        "<a href='${base_url}${id}' target='_blank'>${id}</a>"
                    }.join(', ') // Join the hyperlinks into a comma-separated string
                }
                echo "ELM_ID: ${ELM_ID}"
                
                // Write the content to a file
                // syntax: writeFile file: 'output.txt', text: output, append: true
                writeFile file: "DIFF_OF_${TAG_NAME}_AND_${PREVIOUS_RELEASED_TAG_NAME}.html", text: tagchanges_stat
                writeFile file: "SUMMARY_OF_${TAG_NAME}_AND_${PREVIOUS_RELEASED_TAG_NAME}.html", text: diffchanges

                def htmlTemplate = readFile '/data/tool/prod_success_email_template.html'
                // Replace placeholders with actual Jenkins variables
                // Bellow variabels are common for both Production and Developers Environment
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BUILD_NUMBER\}/, "${BUILD_NUMBER}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BUILD_URL\}/, "${BUILD_URL}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{JOB_BASE_NAME\}/, "${JOB_BASE_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{JOB_NAME\}/, "${JOB_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{PROJECT_NAME\}/, "${PROJECT_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BUILD_DISPLAY_NAME\}/, "${BUILD_DISPLAY_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{TAG_NAME\}/, "${TAG_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{APP_SW_VERSION\}/, "${APP_SW_VERSION}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BL_SW_VERSION\}/, "${BL_SW_VERSION}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{CRC_VALUE\}/, "${CRC_VALUE}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BINARY_SIZE\}/, "${BINARY_SIZE}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{MD5SUM\}/, "${MD5SUM}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{CONTAINER_NAME\}/, "${CONTAINER_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{GIT_COMMIT\}/, "${GIT_COMMIT}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{GIT_AUTHOR\}/, "${GIT_AUTHOR_EMAIL}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BATTERY_PACK_VERSION\}/, "${BATTERY_PACK_VERSION}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{CONTAINER_DIR\}/, "${CONTAINER_DIR}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{ELM_ID\}/, "${ELM_ID}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{meta_info_line_1\}/, "${META_INFO_LINE_1}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{meta_info_line_2\}/, "${META_INFO_LINE_2}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{Software_Part_Number\}/, "${Software_Part_Number}")
                


                // Bellow variabels are for Production Environment
                htmlTemplate = htmlTemplate.replaceAll(/\$\{logStatOutput_short\}/, "${logStatOutput_short}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{diffchanges\}/, "${diffchanges}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{PREVIOUS_RELEASED_TAG_NAME\}/, "${PREVIOUS_RELEASED_TAG_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{tagchanges_stat\}/, "${tagchanges_stat}")

                dir("${WORKSPACE}") {
                    writeFile file: "${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.html", text: htmlTemplate
                    sh """
wkhtmltopdf "${WORKSPACE}/${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.html" "${WORKSPACE}/${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.pdf" || true
                    """
                    sh """
                        # UPLOADING PDF FILE to AZURE
                        azcopy copy "${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.pdf" "https://${AZURE_STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${CONTAINER_DIR}/${PROJECT_NAME}/${APP_SW_VERSION}_%23${BUILD_NUMBER}/?${SAS_KEY_INT}" --recursive=true || true
                    """
                }

                archiveArtifacts artifacts: "${OUT_DIR}/${PROJECT_NAME}/*.hex, ${OUT_DIR}/${PROJECT_NAME}/*.elf, **/*.pdf, **/*.txt", followSymlinks: false, onlyIfSuccessful: true

                emailext \
                subject: """[VCU-${PROJECT_NAME}][PROD]: Release Info: ${APP_SW_VERSION} --> #${BUILD_NUMBER}""",
                body: """ ${htmlTemplate} """,
                to: """${EMAIL_TO}""",
                attachmentsPattern: """**/${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.pdf"""
                //attachmentsPattern: """${OUT_DIR}/${PROJECT_NAME}/*.hex, **/${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.pdf"""

                def djangoDatabaseUpdate = (env.DJANGO_DATABASE_UPDATE != null && env.DJANGO_DATABASE_UPDATE != '') ? env.DJANGO_DATABASE_UPDATE.toBoolean() : false
                // Run Django management command with Jenkins parameters
                if (djangoDatabaseUpdate.toBoolean()) {
                    try {
                        echo "Preparing to execute Django management command..."
                        sh """
                        cd /home/swcocuser/django_web_app/django

                        . ../venv/bin/activate

                        # Ensure the changelog file exists before running the command
                        if [ ! -f "${WORKSPACE}/${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.pdf" ]; then
                            echo "Attachment file not found: ${WORKSPACE}/${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.pdf"
                            exit 1
                        fi

                        python manage.py releasedetails_vcu_create_instance \
                            --project="${PROJECT_NAME}" \
                            --tag_name="${TAG_NAME}" \
                            --app_version="${APP_SW_VERSION}" \
                            --bl_version="${BL_SW_VERSION}" \
                            --battery_pack="${BATTERY_PACK_VERSION}" \
                            --loc="${logStatOutput_short}" \
                            --commit_Id="${GIT_COMMIT}" \
                            --binary_path="${CONTAINER_NAME}/${CONTAINER_DIR}/${PROJECT_NAME}/${BUILD_DISPLAY_NAME}" \
                            --build_url="${BUILD_URL}" \
                            --build_id="${BUILD_DISPLAY_NAME}" \
                            --release_date="${DATE}" \
                            --bugs="${DATABASE_ELM_ID}" \
                            --email_attached_change_log="${WORKSPACE}/${PROJECT_NAME}_${TAG_NAME}_${DATE}_#${BUILD_NUMBER}.pdf"
                        """
                    } catch (Exception e) {
                        error "Django database update failed: ${e.message}"
                    }
                } else {
                    echo "No need to update Django database"
                }
            }
        }

        failure {
            // Actions to be performed when the pipeline fails
            echo "Pipeline failed! Sending notifications..."
            script {
                //def changelogTable = collectChangelogTable()
                def changelogTable = collectChangelogTableForPR()
                def htmlTemplate = readFile '/data/tool/failure_email_template.html'
                // Replace placeholders with actual Jenkins variables
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BUILD_NUMBER\}/, "${BUILD_NUMBER}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{BUILD_URL\}/, "${BUILD_URL}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{FAILED_STAGE\}/, "${FAILED_STAGE}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{JOB_NAME\}/, "${JOB_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{JOB_BASE_NAME\}/, "${JOB_BASE_NAME}")
                htmlTemplate = htmlTemplate.replaceAll(/\$\{changelogTable\}/, "${changelogTable}")

                emailext \
                subject: """${PROJECT_NAME}[PROD] --> Jenkins Pipeline - Build Failed --> ${JOB_NAME} #${BUILD_NUMBER}""",
                body: """ ${htmlTemplate} """,
                to: """${EMAIL_TO}""",
                attachLog: true
                //emailext (to: 'narendra.babu@tvsmotor.com', subject: "Email Report", body: htmlTemplate, mimeType: 'text/html');
            }
        }
    }
}

// This Function is useful for when you trigger job using webhook for commits or any push
def collectChangelogTable() {
    // Collect changelog information
    def changelogTable = "<table border='1'><tr><th>Commit ID</th><th>Author</th><th>Timestamp</th><th>Commit Message</th><th>Affected Files</th></tr>"

    currentBuild.changeSets.each { changeLogSet ->
        changeLogSet.each { entry ->
            changelogTable += "<tr>"
            changelogTable += "<td>${entry.commitId}</td>"
            changelogTable += "<td>${entry.author.fullName}</td>"
            changelogTable += "<td>${new Date(entry.timestamp)}</td>"
            changelogTable += "<td>${entry.msg}</td>"
            changelogTable += "<td>${entry.affectedFiles.collect { it.path }.join(', ')}</td>"
            changelogTable += "</tr>"
        }
    }

    changelogTable += "</table>"
    return changelogTable
}

// This Function is useful for when you trigger job using webhook for pushing pull request
def collectChangelogTableForPR() {
    // Collect changelog information for pull request
    def changelogTable = "<table border='1'><tr><th>Commit ID</th><th>Author</th><th>Timestamp</th><th>Commit Message</th><th>Affected Files</th></tr>"

    // Accessing pull request changes
    def changeSets = currentBuild.changeSets
    echo "changeSets, ${changeSets}"
    if (changeSets != null && changeSets.size() > 0) {
        def pullRequest = changeSets[0]
        pullRequest.each { entry ->
            changelogTable += "<tr>"
            changelogTable += "<td>${entry.commitId}</td>"
            changelogTable += "<td>${entry.author.fullName}</td>"
            changelogTable += "<td>${new Date(entry.timestamp)}</td>"
            changelogTable += "<td>${entry.msg}</td>"
            changelogTable += "<td>${entry.affectedFiles.collect { it.path }.join(', ')}</td>"
            //def commitId = entry.commitId
            //def commit_id = commitId.take(12).trim()
            //def logStatOutput_short_commit = sh(script: "git show --shortstat --oneline ${entry.commitId} | sed -n 2p", returnStdout: true).trim()
            //echo "logStatOutput_short_commit: ${logStatOutput_short_commit}"
            //changelogTable += "<td></td>"
            changelogTable += "</tr>"
        }
    } else {
        changelogTable += "<tr><td colspan='5'>No changes found for the pull request.</td></tr>"
    }

    changelogTable += "</table>"
    return changelogTable
}

def getShortStat(commitId) {
    def gitCommand = "git show --shortstat --oneline ${commitId} | sed -n 2p"
    def gitCommand_short = sh(script: 'git show --shortstat --oneline ${commitId}', returnStdout: true).trim()
    return gitCommand_short
}

// This Function is useful for fecthing the summary of bewteen the tags. i.e total file changes and addtions/deletions
def calculateAndSendDiff(gitDiff, logStatOutput_short) {
    // Use 'git diff' to get information about modified files and changes
    def tagchanges = "<table border='1'><tr><th>Affected Files</th><th>Additions</th><th>Deletions</th></tr>"
    
    //def gitDiff = sh(script: "git diff ${tag1}..${tag2} --numstat", returnStdout: true).trim()
    def changes = gitDiff.split('\n').collect { line ->
        def parts = line.split('\t')
        //echo "parts: ${parts}"
        def additions
        def deletions
        try {
            additions = parts[0].toInteger()
        } catch (NumberFormatException e) {
            // Handle the case where parts[0] cannot be converted to an integer
            println "Error: ${e.message}"
            // You can choose to assign a default value or handle the error in another way
            additions = 0 // Assign a default value of 0
        }
        try {
            deletions = parts[1].toInteger()
        } catch (NumberFormatException e) {
            // Handle the case where parts[0] cannot be converted to an integer
            println "Error: ${e.message}"
            // You can choose to assign a default value or handle the error in another way
            deletions = 0 // Assign a default value of 0
        }
        //def additions = parts[0].toInteger()
        //def deletions = parts[1].toInteger()
        def file = parts[2]
        [file: file, additions: additions, deletions: deletions]
    }

    // Display the changes
    echo "Modified Files and Changes between the tags:"
    changes.each { change ->
        //echo "change, ${change}"
        //echo "changes, ${changes}"
        tagchanges += "<tr>"
        tagchanges += "<td>${change.file}</td>"
        tagchanges += "<td>${change.additions}</td>"
        tagchanges += "<td>${change.deletions}</td>"
        tagchanges += "</tr>"
        //echo "File: ${change.file}, Additions: ${change.additions}, Deletions: ${change.deletions}"
    }

    // Extracting relevant details using regex for shortstat
    def shortstatDetails = [:]
    def parts = logStatOutput_short.split(",")
    shortstatDetails.totalFilesChanged = parts[0].trim().tokenize()[0]
    shortstatDetails.totalInsertions = parts[1].trim().tokenize()[0]
    shortstatDetails.totalDeletions = parts[2].trim().tokenize()[0]

    echo "shortstatDetails: ${shortstatDetails}"
    tagchanges += """
    <tr>
    <td style="color:blue">Total Files Changed: <b>${shortstatDetails.totalFilesChanged}<b></td>
    <td style="color:green">Total Additions: <b>${shortstatDetails.totalInsertions}</b></td>
    <td style="color:purple">Total Deletions: <b>${shortstatDetails.totalDeletions}</b></td>
    </tr>
    """

    tagchanges += "</table>"
    // Send an email with the changes
    return tagchanges
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

// Close HTML table
htmlTable += "</table>"
    return [htmlTable, elmIDs]
}

// Fetching SW_VERSION from makefile. i.e. APP_SW_VERSION and BOOTLOADER_SW_VERSION.
def softwareVersionFetching(SW_VERSION_FILE, searchString) {
    def commandOne = "${SW_VERSION_FILE} | grep '${searchString}'"
    //def commandTwo = "awk -F'[(]' '{print \$2}'|awk -F'x' '{print \$2}'|tr -d ') '"
    def commandTwo = "tr -d '()' |awk -F'x' '{print \$2}'|awk -F' ' '{print \$1}'|tr -d ' '"
    
    dir ("${WORKSPACE}") {
        script {
            output = sh(script:"""cat ${commandOne} | ${commandTwo}""", returnStdout: true).trim()
        }
    }
    echo """output: ${output}"""
    return output
}

// Define the function
def convertHexToAscii(projectCodes) {
    // Convert hex values to ASCII characters
    return projectCodes.collect { code ->
        // Extract the hexadecimal part, ignoring the last character ('U')
        def hexPart = code.substring(0, 2)
        // Convert hex to decimal
        def decimalValue = Integer.parseInt(hexPart, 16)
        // Convert decimal to ASCII character
        (char) decimalValue
    }.join('').trim() // Join the characters with an empty delimiter
}

def generateCommitDetailsHTML(commitIDs) {
    def elmIDs = [] // Store all extracted numbers (ELM IDs)
    def htmlTable = '<table border="1">'
    htmlTable += '<tr><th rowspan="2">Commit ID</th><th rowspan="2">Author</th><th rowspan="2">Commit Message</th><th rowspan="2">Time Stamp</th><th colspan="3">Changes</th><th colspan="3">Affected Files</th></tr>'
    htmlTable += '<tr><th>Total Files Changed</th><th>Total Insertions</th><th>Total Deletions</th></tr>'
    
    commitIDs.each { commitID ->
        def commitDetail = sh(script: "git show --format='%H|!%s|!%an|!%ai' --numstat ${commitID}", returnStdout: true).trim()
        def commit_message = sh(script:"git log --format=%B -n 1 ${commitID}", returnStdout: true).trim()
        def commitLines = commitDetail.split('\n')
        def commitInfo = commitLines[0].split("\\|!")
        def commitId = commitInfo[0].trim()
        def commitMessage = commit_message.replaceAll("\n", "<hr>").replaceAll("\r", "<hr>")
        //Extract the ELM ID's from the commit message (4 to 8 digits)
        def numberMatches = (commitMessage ?: "").findAll(/\b\d{4,8}\b/)
        // Add all matches to elmIDs
        elmIDs.addAll(numberMatches)

        //def commitMessage = commitInfo[1].trim().replaceAll("\n", "<br>").replaceAll("\r", "<br>")
        //def commitMessage = commitInfo[1].replaceAll("\n", "<br>").replaceAll("\t", "<br>")
        def author = commitInfo[2].replaceAll(/\s*\(.*?\)/, "").trim()
        def timeStamp = commitInfo[3].trim()
        
        def filesChanged = 0
        def insertions = 0
        def deletions = 0
        def affectedFiles = []
        def fileDetails = [:] // Map to store file-wise insertions and deletions
        
        commitLines.each { line ->
            if (line =~ /^\d+\s+\d+/) {
                def numStat = line.tokenize('\t')
                filesChanged++
                insertions += numStat[0].toInteger()
                deletions += numStat[1].toInteger()
                
                // Store file-wise insertions and deletions
                def fileName = numStat[2]
                def fileInsertions = numStat[0].toInteger()
                def fileDeletions = numStat[1].toInteger()
                fileDetails[fileName] = [insertions: fileInsertions, deletions: fileDeletions]
                affectedFiles.add(fileName)
            } else if (line =~ /^[AMD]\s+(.*)/) {
                def fileChange = line.split('\t')[1]
                affectedFiles.add(fileChange)
            }
        }
        def commitURL = "${REPOSITORY_LINK}" + "/commits/" + "${commitId}"
        htmlTable += "<tr>"
        htmlTable += "<td><a href='${commitURL}'>${commitId.take(10).trim()}</a></td>"
        htmlTable += "<td>${author}</td>"
        htmlTable += "<td>${commitMessage}</td>"
        htmlTable += "<td>${timeStamp}</td>"
        htmlTable += "<td>${filesChanged}</td>"
        htmlTable += "<td>${insertions}</td>"
        htmlTable += "<td>${deletions}</td>"
        htmlTable += "<td>${affectedFiles.join('<hr>')}</td>"
        htmlTable += "</tr>"
    }
    
    htmlTable += '</table>'
    return [htmlTable, elmIDs]
}
