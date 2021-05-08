# base64 解密 创建文件
decodebase64MapperXmlFile() {
  decodeBase64AndCreateFile "${BASE64_TXT_AND_PATH}"
}

#执行调用bean的信息
doStartMybatisMaperReloadCommand() {
  executeArthasCommand "${HOME}/opt/arthas/mybatisMapperXmlReloadResult.out" "${arthasIdeaPluginMybatisMapperXmlReloadCommand}"
}

# 获取执行的结果
getMybatisMapperReloadCommandResult() {
  local mybatisMapperReloadCommandResult=$(cat $HOME/opt/arthas/mybatisMapperXmlReloadResult.out | grep -E "@Boolean\[true\]")
  if [ -z "$mybatisMapperReloadCommandResult" ]; then
    banner_simple $(echo $(tput setaf 1)arthas idea plugin mybatis mapper xml reload error $(tput sgr0))
  else
    banner_simple "arthas idea plugin mybatis mapper xml reload success"
  fi
}

main() {

  banner_simple "arthas idea plugin begin;start script path: $(pwd)/arthas-idea-plugin-mybatis-mapper-xml-reload.sh"

  check_permission

  installArthas

  if [ $? -ne 0 ]; then
    exit_on_err 1 "arthas install as.sh script error"
  fi

  decodebase64MapperXmlFile
  if [ $? -ne 0 ]; then
    exit_on_err 1 "arthas idea plugin decodebase64MapperXmlFile error"
  fi

  if [ -z "${SELECT_VALUE}" ]; then
    reset_for_env
    select_pid
    SELECT_VALUE="${TARGET_PID}"
  fi

  if [ -z "${SELECT_VALUE}" ]; then
    exit_on_err 1 "select target process by classname or JARfilename Target pid is empty"
  fi

  banner_simple "first: get spring static spring context class1oader hash value"
  getFirstClassLoaderHashValue
  if [ -z "${CLASSLOADER_HASH_VALUE}" ]; then
    exit_on_err 1 "not found classloader hash value"
  fi

  banner_simple "last: invoke spring bean to reload mybatis mapper xml file"
  doStartMybatisMaperReloadCommand

  getMybatisMapperReloadCommandResult
}

main "${@}"
