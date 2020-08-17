#!/usr/bin/env bash

# Usage: banner_simple "my title"
function banner_simple() {
  local msg="* $* *"
  local edge=$(echo "$msg" | sed 's/./*/g')
  echo "$edge"
  echo "$(tput bold)$msg$(tput sgr0)"
  echo "$edge"
  echo
}
banner_simple "arthas idea plugin redefine beigin"

if [ ! -d "$HOME/opt/arthas" ]; then
  banner_simple "make dir ~/opt/arthas"
  mkdir -p $HOME/opt/arthas
fi

# download arthas
if [ ! -f "$HOME/opt/arthas/arthas-boot.jar" ]; then
  banner_simple "down load arthas"
  curl -sL https://arthas.aliyun.com/arthas-boot.jar -o $HOME/opt/arthas/arthas-boot.jar
fi

createFile() {
  if [ ! -f $1 ]; then
    mkdir -p "$(dirname "$1")" && touch $1 >/dev/null 2>&1 && echo "File $1 created." || echo "Error: Failed to create $1 files."
  fi
}

## base64ClassFile|/tmp/test.txt,bas65File|/tmp/test1.txt
bash64FileAndPathList="${arthasIdeaPluginBase64AndPathCommand}"
commaArraybash64FilePath=(${bash64FileAndPathList//\,/ })
for i in "${!commaArraybash64FilePath[@]}"; do
  verticalArraySingleBash64FileAndPath=(${commaArraybash64FilePath[i]//\|/ })
  createFile ${verticalArraySingleBash64FileAndPath[1]}
  echo ${verticalArraySingleBash64FileAndPath[0]} | base64 --decode >${verticalArraySingleBash64FileAndPath[1]}
  banner_simple "base64Class decode to path ${verticalArraySingleBash64FileAndPath[1]}"
done

banner_simple "redefine command:${arthasIdeaPluginRedefineCommand}"

java -jar ~/opt/arthas/arthas-boot.jar --select ${arthasIdeaPluginApplicationName} -c "${arthasIdeaPluginRedefineCommand}" >/tmp/redefine.out
cat /tmp/redefine.out
redefineResult=$(cat /tmp/redefine.out | grep "success")
if [ -z "$redefineResult" ]; then
  banner_simple $(echo $(tput setaf 1)arthas idea plugin redefine error $(tput sgr0))
else
  banner_simple "arthas idea plugin redefine success"
fi
