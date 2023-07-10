function checkFlatpakExist() {
  if [ -z "$(flatpak info "$1" 2>/dev/null | grep 'ID: ')" ]; then
    return 0
  else
    return 1
  fi
}

function main() {
  checkFlatpakExist org.fcitx.Fcitx5
  fcitxExist=$?
  checkFlatpakExist org.fcitx.Fcitx5.Addon.ChineseAddons
  fcitxAddonsExist=$?
  if [ "$fcitxExist" == 1 ] && [ "$fcitxAddonsExist" == 1 ]; then
    echo "Installed"
  else
    echo "Uninstalled"
  fi
}

main