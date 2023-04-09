#!/usr/bin/env bash
TARGET_PID=
SELECT_VALUE=${arthasIdeaPluginApplicationName}

#arthas package zip download url = https://arthas.aliyun.com/download/latest_version?mirror=aliyun
ARTHAS_PACKAGE_ZIP_DOWNLOAD_URL="${arthasPackageZipDownloadUrl}"

#执行中获取到的hash value的变量
CLASSLOADER_HASH_VALUE=
#sc -d 命令
SC_COMMAND="${SC_COMMAND}"
#base64 文件地址
BASE64_TXT_AND_PATH=${BASE64_TXT_AND_PATH}

# SYNOPSIS
#   rreadlink <fileOrDirPath>
# DESCRIPTION
#   Resolves <fileOrDirPath> to its ultimate target, if it is a symlink, and
#   prints its canonical path. If it is not a symlink, its own canonical path
#   is printed.
#   A broken symlink causes an error that reports the non-existent target.
# LIMITATIONS
#   - Won't work with filenames with embedded newlines or filenames containing
#     the string ' -> '.
# COMPATIBILITY
#   This is a fully POSIX-compliant implementation of what GNU readlink's
#    -e option does.
# EXAMPLE
#   In a shell script, use the following to get that script's true directory of origin:
#     trueScriptDir=$(dirname -- "$(rreadlink "$0")")
rreadlink() (# Execute the function in a *subshell* to localize variables and the effect of `cd`.

  target=$1 fname= targetDir= CDPATH=

  # Try to make the execution environment as predictable as possible:
  # All commands below are invoked via `command`, so we must make sure that
  # `command` itself is not redefined as an alias or shell function.
  # (Note that command is too inconsistent across shells, so we don't use it.)
  # `command` is a *builtin* in bash, dash, ksh, zsh, and some platforms do not
  # even have an external utility version of it (e.g, Ubuntu).
  # `command` bypasses aliases and shell functions and also finds builtins
  # in bash, dash, and ksh. In zsh, option POSIX_BUILTINS must be turned on for
  # that to happen.
  {
    \unalias command
    \unset -f command
  } >/dev/null 2>&1
  [ -n "$ZSH_VERSION" ] && options[POSIX_BUILTINS]=on # make zsh find *builtins* with `command` too.

  while :; do # Resolve potential symlinks until the ultimate target is found.
    [ -L "$target" ] || [ -e "$target" ] || {
      command printf '%s\n' "ERROR: '$target' does not exist." >&2
      return 1
    }
    command cd "$(command dirname -- "$target")" # Change to target dir; necessary for correct resolution of target path.
    fname=$(command basename -- "$target")       # Extract filename.
    [ "$fname" = '/' ] && fname=''               # !! curiously, `basename /` returns '/'
    if [ -L "$fname" ]; then
      # Extract [next] target path, which may be defined
      # *relative* to the symlink's own directory.
      # Note: We parse `ls -l` output to find the symlink target
      #       which is the only POSIX-compliant, albeit somewhat fragile, way.
      target=$(command ls -l "$fname")
      target=${target#* -> }
      continue # Resolve [next] symlink target.
    fi
    break # Ultimate target reached.
  done
  targetDir=$(command pwd -P) # Get canonical dir. path
  # Output the ultimate target's canonical path.
  # Note that we manually resolve paths ending in /. and /.. to make sure we have a normalized path.
  if [ "$fname" = '.' ]; then
    command printf '%s\n' "${targetDir%/}"
  elif [ "$fname" = '..' ]; then
    # Caveat: something like /var/.. will resolve to /private (assuming /var@ -> /private/var), i.e. the '..' is applied
    # AFTER canonicalization.
    command printf '%s\n' "$(command dirname -- "${targetDir}")"
  else
    command printf '%s\n' "${targetDir%/}/$fname"
  fi
)

# reset arthas work environment
# reset some options for env
reset_for_env() {
  # if env define the JAVA_HOME, use it first
  # if is alibaba opts, use alibaba ops's default JAVA_HOME
  [ -z "${JAVA_HOME}" ] && [ -d /opt/taobao/java ] && JAVA_HOME=/opt/taobao/java

  if [[ (-z "${JAVA_HOME}") && (-e "/usr/libexec/java_home") ]]; then
    # for mac
    JAVA_HOME=$(/usr/libexec/java_home)
  fi

  if [ -z "${JAVA_HOME}" ]; then
    # try to find JAVA_HOME from java command
    local JAVA_COMMAND_PATH=$(rreadlink $(type -p java))
    JAVA_HOME=$(echo "$JAVA_COMMAND_PATH" | sed -n 's/\/bin\/java$//p')
  fi

  # iterater throught candidates to find a proper JAVA_HOME at least contains tools.jar which is required by arthas.
  if [ ! -d "${JAVA_HOME}" ]; then
    JAVA_HOME_CANDIDATES=($(ps aux | grep java | grep -v 'grep java' | awk '{print $11}' | sed -n 's/\/bin\/java$//p'))
    for JAVA_HOME_TEMP in ${JAVA_HOME_CANDIDATES[@]}; do
      if [ -f "${JAVA_HOME_TEMP}/lib/tools.jar" ]; then
        JAVA_HOME=$(rreadlink "${JAVA_HOME_TEMP}")
        break
      fi
    done
  fi

  if [ -z "${JAVA_HOME}" ]; then
    exit_on_err 1 "Can not find JAVA_HOME, please set \$JAVA_HOME bash env first."
  fi

  # maybe 1.8.0_162 , 11-ea
  local JAVA_VERSION

  local IFS=$'\n'
  # remove \r for Cygwin
  local lines=$("${JAVA_HOME}"/bin/java -version 2>&1 | tr '\r' '\n')
  for line in $lines; do
    if [[ (-z $JAVA_VERSION) && ($line == *"version \""*) ]]; then
      local ver=$(echo $line | sed -e 's/.*version "\(.*\)"\(.*\)/\1/; 1q')
      # on macOS, sed doesn't support '?'
      if [[ $ver == "1."* ]]; then
        JAVA_VERSION=$(echo $ver | sed -e 's/1\.\([0-9]*\)\(.*\)/\1/; 1q')
      else
        JAVA_VERSION=$(echo $ver | sed -e 's/\([0-9]*\)\(.*\)/\1/; 1q')
      fi
    fi
  done

  # when java version less than 9, we can use tools.jar to confirm java home.
  # when java version greater than 9, there is no tools.jar.
  if [[ "$JAVA_VERSION" -lt 9 ]]; then
    # possible java homes
    javaHomes=("${JAVA_HOME%%/}" "${JAVA_HOME%%/}/.." "${JAVA_HOME%%/}/../..")
    for javaHome in ${javaHomes[@]}; do
      toolsJar="$javaHome/lib/tools.jar"
      if [ -f $toolsJar ]; then
        JAVA_HOME=$(rreadlink $javaHome)
        BOOT_CLASSPATH=-Xbootclasspath/a:$(rreadlink $toolsJar)
        break
      fi
    done
    [ -z "${BOOT_CLASSPATH}" ] && exit_on_err 1 "tools.jar was not found, so arthas could not be launched!"
  fi

  echo "[INFO] JAVA_HOME: ${JAVA_HOME}"

  # reset CHARSET for alibaba opts, we use GBK
  [[ -x /opt/taobao/java ]] && JVM_OPTS="-Dinput.encoding=GBK ${JVM_OPTS} "
}

select_pid() {
  local IFS=$'\n'
  CANDIDATES=($(${JAVA_HOME}/bin/jps -l | grep -v sun.tools.jps.Jps | awk '{print $0}'))

  index=0
  suggest=1
  # auto select tomcat/pandora-boot process
  for process in "${CANDIDATES[@]}"; do
    index=$(($index + 1))
    if [ $(echo ${process} | grep -c org.apache.catalina.startup.Bootstrap) -eq 1 ] ||
      [ $(echo ${process} | grep -c com.taobao.pandora.boot.loader.SarLauncher) -eq 1 ]; then
      suggest=${index}
      break
    fi
  done

  index=0
  for process in "${CANDIDATES[@]}"; do
    index=$(($index + 1))
    if [ ${index} -eq ${suggest} ]; then
      echo "* [$index]: ${process}"
    else
      echo "  [$index]: ${process}"
    fi
  done
  echo " "
  echo "$(echo $(tput setaf 1) 请手动选择进程或者idea 预先配置jps -l 工程名称自动执行$(tput sgr0))"
  echo " "

  read choice

  if [ -z ${choice} ]; then
    choice=${suggest}
  fi

  TARGET_PID=$(echo ${CANDIDATES[$(($choice - 1))]} | cut -d ' ' -f 1)
}

# Usage: banner_simple "my title"
banner_simple() {
  local msg="* $* *"
  local edge=$(echo "$msg" | sed 's/./*/g')
  echo "$edge"
  echo "$(tput bold)$msg$(tput sgr0)"
  echo "$edge"
  echo
}

# check arthas permission
check_permission() {
  [ ! -w "${HOME}" ] &&
    exit_on_err 1 "permission denied, ${HOME} is not writable."
}

# exit shell with err_code
# $1 : err_code
# $2 : err_msg
exit_on_err() {
  [[ ! -z "${2}" ]] && echo "${2}" 1>&2
  exit ${1}
}

createFile() {
  if [ ! -f $1 ]; then
    mkdir -p "$(dirname "$1")" && touch $1 >/dev/null 2>&1 && echo "File $1 created." || return 1
  fi
}

installArthas() {
  if [ ! -d "$HOME/opt/arthas" ]; then
    banner_simple "arthas idea plugin make dir $HOME/opt/arthas"
    mkdir -p $HOME/opt/arthas || return 1
  fi
  # download arthas

  if [ ! -f "$HOME/opt/arthas/arthas-agent.jar" ]; then
    # 这里选择判断是否下载zip 包 没有使用 arthas-packaging-latest-version-bin.zip 这个名词 由于手动上传解压到当前目录名词可能不一致，选择一个中性的判断比较灵活
    local temp_target_lib_zip="$HOME/opt/arthas/arthas-packaging-latest-version-bin.zip"
    echo "arthas idea plugin download arthas zip package ${temp_target_lib_zip} download url=${ARTHAS_PACKAGE_ZIP_DOWNLOAD_URL}"
    echo " "
    echo "$(echo $(tput setaf 1)如果网络无法访问 https://arthas.aliyun.com/download/latest_version?mirror=aliyun $(tput sgr0))"
    echo "$(echo $(tput setaf 1)idea设置网络可以访问的arthas 完整zip包的下载地址 或者直接下载解压到服务器 $HOME/opt/arthas 目录 $(tput sgr0))"
    echo "$(echo $(tput setaf 1)如果配置的是oss 存储,arthas 命令 other分组下面 Local File Upload To Oss命令可以上传文件 有效期1年,配置到arthas zip包地址$(tput sgr0))"
    echo " "
    curl -Lk "${ARTHAS_PACKAGE_ZIP_DOWNLOAD_URL}" -o "${temp_target_lib_zip}" || retrun 1
    cd "$HOME/opt/arthas" && unzip -o "${temp_target_lib_zip}"
  fi
  chmod -R +x "$HOME/opt/arthas/" || return 1
}

# execute arthas batch command
# $1 : resultFle
# $2 : command
executeArthasCommand() {
  createFile "${1}"
  # " 里面的 " 要进行转义 \"
  echo $(tput bold)"arthas start command :$JAVA_HOME/bin/java -jar $HOME/opt/arthas/arthas-boot.jar --height 100 --width 200 --select ${SELECT_VALUE}  -c \"${2}\"  | tee ${1}"$(tput sgr0)
  # --height 100 --width 200  the data displayed by automatic execution script is too ugly,to resolve classloaderhashvalue error
  $JAVA_HOME/bin/java -jar $HOME/opt/arthas/arthas-boot.jar --height 100 --width 200 --select ${SELECT_VALUE} -c "${2}" | tee ${1}
}

# decode base64 text and create file
# $1 : base64Text|createFilePath1,base64Text2|createFilePath2
decodeBase64AndCreateFile() {
  bash64FileAndPathList="${1}"
  commaArrayBash64FilePath=(${bash64FileAndPathList//\,/ })
  for i in "${!commaArrayBash64FilePath[@]}"; do
    verticalArraySingleBash64FileAndPath=(${commaArrayBash64FilePath[i]//\|/ })
    createFile ${verticalArraySingleBash64FileAndPath[1]}
    echo ${verticalArraySingleBash64FileAndPath[0]} | base64 --decode >${verticalArraySingleBash64FileAndPath[1]} || return 1
    echo " "
    echo "decode base64 text to path ${verticalArraySingleBash64FileAndPath[1]}"
    echo " "
  done
}

# 获取第一个classloader hash value
getFirstClassLoaderHashValue() {
  local arthasClassLoaderHashValueResult="${HOME}/opt/arthas/classLoaderHashValue.out"
  createFile "${arthasClassLoaderHashValueResult}"
  executeArthasCommand "${arthasClassLoaderHashValueResult}" "${SC_COMMAND}"
  CLASSLOADER_HASH_VALUE=$(cat "${arthasClassLoaderHashValueResult}" | awk '/classLoaderHash/{print $2;}' | head -1)
}
