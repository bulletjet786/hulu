DIST_DIR = /www/wwwroot/static

STARTER_OUTPUT_DIR = ./starter/build/bin/native/debugExecutable
STARTER_VERSION = $(shell $(STARTER_OUTPUT_DIR)/starter.kexe version)
STARTER_PKG_DIR = starter/build/product
STARTER_PKG_NAME = starter.tar.gz
STARTER_PKG = $(STARTER_PKG_DIR)/${STARTER_PKG_NAME}

LET_OUTPUT_DIR = ./let/build/bin/native/debugExecutable
LET_VERSION = $(shell $(LET_OUTPUT_DIR)/let.kexe version)
LET_PKG_DIR = let/build/product
LET_PKG_NAME = let.tar.gz
LET_PKG = $(LET_PKG_DIR)/${LET_PKG_NAME}

PAD_OUTPUT_DIR = ./pad/build/compose/binaries/main/app/pad
PAD_VERSION = $(shell $(PAD_OUTPUT_DIR)/bin/pad version)
PAD_PKG_DIR = ./pad/build/product
PAD_PKG_NAME = pad.tar.gz
PAD_PKG = $(PAD_PKG_DIR)/${PAD_PKG_NAME}

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
	mkdir -p ${STARTER_PKG_DIR}
	tar -czvf $(STARTER_PKG) -C $(STARTER_OUTPUT_DIR) .
	@echo "start upload starter:${STARTER_VERSION} ..."
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/hulu/starter/${STARTER_VERSION}/
	scp -i ~/.tencentcloud.pem ${STARTER_PKG} root@150.158.135.143:${DIST_DIR}/hulu/starter/${STARTER_VERSION}/${STARTER_PKG_NAME}
	@echo "upload starter:${STARTER_VERSION} finished"

.PHONY:
dist_let:
	./gradlew let:clean
	./gradlew let:nativeBinaries
	mkdir -p ${LET_PKG_DIR}
	tar -czvf $(LET_PKG) -C $(LET_OUTPUT_DIR) .
	@echo "start upload hulu let:${LET_VERSION} ..."
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/hulu/let/${LET_VERSION}/
	scp -i ~/.tencentcloud.pem ${LET_PKG} root@150.158.135.143:${DIST_DIR}/hulu/let/${LET_VERSION}/${LET_PKG_NAME}
	@echo "upload upload hulu let:${LET_VERSION} finished"

.PHONY:
dist_pad:
	./gradlew pad:clean
	./gradlew pad:createDistributable
	mkdir -p  $(PAD_PKG_DIR)
	tar -czvf $(PAD_PKG) -C $(PAD_OUTPUT_DIR) .
	@echo "start upload hulu pad:${PAD_VERSION} ..."
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/hulu/pad/${PAD_VERSION}/
	scp -i ~/.tencentcloud.pem ${PAD_PKG} root@150.158.135.143:${DIST_DIR}/hulu/pad/${PAD_VERSION}/${PAD_PKG_NAME}
	@echo "upload upload hulu pad:${PAD_VERSION} finished"

.PHONY:
dist_hulu: dist_let dist_pad dist_starter