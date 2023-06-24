#!/usr/bin/env bash
set -o errexit

# configuration
HULU_ENDPOINT="http://150.158.135.143:8181"
CURRENT_DIR=$(pwd)

function checkRoot() {
  set -e

  echo "检查所需要的权限 ... "
  if [ $UID -ne 0 ]; then
    echo "你应该使用root用户来执行这个脚本哦。不会？我教你啊：在命令前增加 sudo 哦。"
    exit
  fi
  echo "检查通过！"
}

function installDependencies() {
  echo "正在安装依赖 ..."
  yes | pacman -Sy --needed libxcrypt-compat
  yes | pacman -Sy --needed jq
  echo "依赖安装完成。"
}

function cleanOldVersion() {
  rm -rf /opt/fun.deckz/*
}

function prepare() {
  echo "正在准备安装目录 ..."
  mkdir -p /opt/fun.deckz/hulu
  mkdir -p /opt/fun.deckz/hulu/starter
  mkdir -p /opt/fun.deckz/hulu/let
  mkdir -p /opt/fun.deckz/hulu/pad
  mkdir -p /opt/fun.deckz/hulu/data
  mkdir -p /opt/fun.deckz/hulu/etc
  mkdir -p /opt/fun.deckz/hulu/var
  echo "安装目录准备完成。"
}

function installPackage() {
  set -e

  # get hulu package download url
  echo "正在查询最新的 宝葫芦 版本 ..."
  VERSION_RESPONSE=$(curl -X POST "$HULU_ENDPOINT"/version/latest -H "Content-Type: application/json" \
    -d '{"starter":{"name":"starter","version":"0.0.1"},"let":{"name":"let","version":"0.0.1"},"pad":{"name":"pad","version":"0.0.1"}}')

  if [ "$(echo "$VERSION_RESPONSE" | jq .status.code)" != 0 ]; then
    echo "查询最新的 宝葫芦 版本失败啦。退出安装啦。"
    exit
  fi

  echo "正在安装 宝葫芦 ..."
  STARTER_DOWNLOAD_URL=$(echo "$VERSION_RESPONSE" | jq .data.starter.downloadUrl | sed -e 's/"//g')
  LET_DOWNLOAD_URL=$(echo "$VERSION_RESPONSE" | jq .data.let.downloadUrl | sed -e 's/"//g')
  PAD_DOWNLOAD_URL=$(echo "$VERSION_RESPONSE" | jq .data.pad.downloadUrl | sed -e 's/"//g')

  # TODO: wget test failed
  cd /opt/fun.deckz/hulu/starter && wget "$STARTER_DOWNLOAD_URL" && tar -zxf starter.tar.gz && rm -rf starter.tar.gz
  cd /opt/fun.deckz/hulu/let && wget "$LET_DOWNLOAD_URL" && tar -zxf let.tar.gz && rm -rf let.tar.gz
  cd /opt/fun.deckz/hulu/pad && wget "$PAD_DOWNLOAD_URL" && tar -zxf pad.tar.gz && rm -rf pad.tar.gz

  cd "$CURRENT_DIR" || exit

  # install service
}

function main() {
  echo "宝葫芦 现在开始安装啦 ... "
  checkRoot
  installDependencies
  prepare
  installPackage
  echo "宝葫芦 成功啦！体验一下吧！"
}

main


