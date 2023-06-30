function checkRoot() {
  echo "检查所需要的权限 ... "
  if [ $UID -ne 0 ]; then
    echo "你应该使用root用户来执行这个脚本哦。不会？我教你啊：在命令前增加 sudo 哦。"
    exit
  fi
  echo "检查通过！"
}

function uninstall() {
  rm -rf /opt/fun.deckz/hulu
  rm -rf /etc/systemd/system/fun.deckz.hulu.service
}

function main() {
  echo "开始卸载！"
  checkRoot
  uninstall
  echo "卸载完成！"
}

main