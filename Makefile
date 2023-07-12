DIST_DIR = /www/wwwroot/static
#WORK_DIR = /home/deck/tmp/fun.deckz/hulu
WORK_DIR = /opt/fun.deckz/hulu

STARTER_OUTPUT_DIR = ./starter/build/bin/native/debugExecutable
STARTER_VERSION = $(shell $(STARTER_OUTPUT_DIR)/starter.kexe version)
STARTER_PKG_DIR = starter/build/product
STARTER_PKG_NAME = starter.tar.gz
STARTER_PKG = $(STARTER_PKG_DIR)/${STARTER_PKG_NAME}

LET_OUTPUT_DIR = ./let/build/bin/native/debugExecutable
# TODO: have bug, should run after build
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
		"cd /app/fun.deckz/hulu/panel; cat application.pid | xargs kill -9 ; nohup java -jar panel.jar > output.log 2>&1 &"

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
	@echo "start upload let:${LET_VERSION} ..."
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/hulu/let/${LET_VERSION}/
	scp -i ~/.tencentcloud.pem ${LET_PKG} root@150.158.135.143:${DIST_DIR}/hulu/let/${LET_VERSION}/${LET_PKG_NAME}
	@echo "upload let:${LET_VERSION} finished"

.PHONY:
dist_pad:
	./gradlew pad:clean
	./gradlew pad:createDistributable
	mkdir -p  $(PAD_PKG_DIR)
	tar -czvf $(PAD_PKG) -C $(PAD_OUTPUT_DIR) .
	@echo "start upload pad:${PAD_VERSION} ..."
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 mkdir -p ${DIST_DIR}/hulu/pad/${PAD_VERSION}/
	scp -i ~/.tencentcloud.pem ${PAD_PKG} root@150.158.135.143:${DIST_DIR}/hulu/pad/${PAD_VERSION}/${PAD_PKG_NAME}
	@echo "upload pad:${PAD_VERSION} finished"

.PHONY:
dist_hulu: dist_let dist_pad dist_starter

.PHONY:
install_let:
	cd $(WORK_DIR)/let && wget http://150.158.135.143:7777/hulu/let/0.0.1/let.tar.gz
	cd $(WORK_DIR)/let && tar -zxf let.tar.gz
	cd $(WORK_DIR)/let && rm -rf let.tar.gz

.PHONY:
install_pad:
	cd $(WORK_DIR)/pad && wget http://150.158.135.143:7777/hulu/pad/0.0.1/pad.tar.gz
	cd $(WORK_DIR)/pad && tar -zxf pad.tar.gz
	cd $(WORK_DIR)/pad && rm -rf pad.tar.gz

.PHONY:
install_starter:
	cd $(WORK_DIR)/starter && wget http://150.158.135.143:7777/hulu/starter/0.0.2/starter.tar.gz
	cd $(WORK_DIR)/starter && tar -zxf starter.tar.gz
	cd $(WORK_DIR)/starter && rm -rf starter.tar.gz

.PHONY:
install_hulu: install_starter install_let install_pad

# TODO: bug can't cp, use rm and cp, todo: test

.PHONY:
l_install_let:
	./gradlew let:clean
	./gradlew let:nativeBinaries
	rm -rf $(WORK_DIR)/let/*
	cp ${LET_OUTPUT_DIR}/* $(WORK_DIR)/let/

.PHONY:
l_install_starter:
	./gradlew starter:clean
	./gradlew starter:nativeBinaries
	rm -rf $(WORK_DIR)/starter/*
	cp ${STARTER_OUTPUT_DIR}/*  $(WORK_DIR)/starter/

.PHONY:
l_install_pad:
	./gradlew pad:clean
	./gradlew pad:createDistributable
	rm -rf $(WORK_DIR)/pad/*
	cp -r ${PAD_OUTPUT_DIR}/* $(WORK_DIR)/pad/

.PHONY:
l_install_extension:
	cp -r let/src/nativeMain/resources/extensions /opt/fun.deckz/hulu/data

.PHONY:
l_restart_hulu:
	systemctl restart fun.deckz.hulu.service