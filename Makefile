STARTER_EXE = ./starter/build/bin/native/debugExecutable/starter.kexe
STARTER_VERSION = $(shell $(STARTER_EXE) version)
HULU_LET_EXE = ./let/build/bin/native/debugExecutable/let.kexe
HULU_LET_VERSION = $(shell $(HULU_LET_EXE) version)
HULU_PAD_EXE = ./pad/build/bin/native/debugExecutable/pad.kexe
HULU_PAD_VERSION = $(shell $(HULU_PAD_EXE) version)
DIST_DIR = /www/wwwroot/static

.PHONY:
update_panel:
	./gradlew panel:clean
	./gradlew panel:bootJar
	scp -i ~/.tencentcloud.pem ./panel/build/libs/*.jar root@150.158.135.143:/app/fun.deckz/hulu/panel/panel.jar
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 \
		"cd /app/fun.deckz/hulu/panel; kill -9 < application.pid;nohup java -jar panel.jar > output.log 2>&1 &"

.PHONY:
dist_starter:
	./gradlew starter:clean
	./gradlew starter:nativeBinaries
	@echo "start upload starter:${STARTER_VERSION} ..."
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/starter/${STARTER_VERSION}/
	scp -i ~/.tencentcloud.pem ${STARTER_EXE} root@150.158.135.143:${DIST_DIR}/starter/${STARTER_VERSION}/starter.kexe
	@echo "upload starter:${STARTER_VERSION} finished"

.PHONY:
dist_hulu:
	./gradlew let:clean
	./gradlew let:nativeBinaries
#	./gradlew pad:clean
#	./gradlew pad:packageDistributionForCurrentOS
	@echo "start upload hulu let:${HULU_LET_VERSION} ..."
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/hulu/let/${HULU_LET_VERSION}/
	scp -i ~/.tencentcloud.pem ${HULU_LET_EXE} root@150.158.135.143:${DIST_DIR}/hulu/let/${HULU_LET_VERSION}/let.kexe
	@echo "upload upload hulu let:${HULU_LET_VERSION} finished"
#	@echo "start upload hulu pad:${HULU_PAD_VERSION} ..."
#	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/hulu/pad/${HULU_PAD_VERSION}/
#	scp -i ~/.tencentcloud.pem ${HULU_PAD_EXE} root@150.158.135.143:${DIST_DIR}/hulu/pad/${HULU_PAD_VERSION}/pad.kexe
#	@echo "upload upload hulu pad:${HULU_PAD_VERSION} finished"
