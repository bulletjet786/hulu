.PHONY:
update_panel:
	./gradlew panel:clean
	./gradlew panel:bootJar
	scp -i ~/.tencentcloud.pem ./panel/build/libs/*.jar root@150.158.135.143:/app/fun.deckz/hulu/panel/panel.jar
	ssh -i ~/.tencentcloud.pem root@150.158.135.143 \
		"cd /app/fun.deckz/hulu/panel; kill -9 < application.pid;nohup java -jar panel.jar > output.log 2>&1 &"

.PHONY:
connect_mysql:
	mycli -h 127.0.0.1 -u root -p'password'