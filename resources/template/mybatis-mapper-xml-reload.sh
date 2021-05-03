SPRING_CONTEXT_CLASSLOADER_HASH_VALUE=
SPRING_CONTEXT_SC_COMMAND="${springContextScCommand}"

# 获取spring context hashvalue
getSpringContextHashValue() {
  banner_simple "first: get spring static spring context class1oader hash value"
  local executeArthasSpringContextReuslt="${HOME}/opt/arthas/springContextScCommandResult.out"
  executeArthasCommand "${executeArthasSpringContextReuslt}" "${SPRING_CONTEXT_SC_COMMAND}"
  SPRING_CONTEXT_CLASSLOADER_HASH_VALUE=$(cat "${executeArthasSpringContextReuslt}" | awk '/classLoaderHash/{print $2;}' | head -1)
  #executeArthasCommand "${executeArthasSpringContextReuslt}" "stop"
}

# 将base64的class文件创建到指定目录
# base64MapperXmlFile|xxxMapperPath,base64MapperXmlFile|xxxMapperPath
decodebase64MapperXmlFile() {
  bash64FileAndPathList="${arthasIdeaPluginBase64MapperXmlAndPath}"
  commaArraybash64FilePath=(${bash64FileAndPathList//\,/ })
  for i in "${!commaArraybash64FilePath[@]}"; do
    verticalArraySingleBash64FileAndPath=(${commaArraybash64FilePath[i]//\|/ })
    createFile ${verticalArraySingleBash64FileAndPath[1]}
    echo ${verticalArraySingleBash64FileAndPath[0]} | base64 --decode >${verticalArraySingleBash64FileAndPath[1]} || return 1
    echo "base64Class decode to path ${verticalArraySingleBash64FileAndPath[1]}"
  done
}

#执行调用bean的信息
doStartMybatisMaperReloadCommand() {
  banner_simple "last: invoke spring bean to reload mybatis mapper xml file"
  executeArthasCommand "${HOME}/opt/arthas/mybatisMapperXmlReloadResult.out" "${arthasIdeaPluginMybatisMapperXmlReloadCommand}"
}

# 获取执行的结果
getMybatisMapperReloadCommandResult() {
  local mybatisMapperReloadCommandResult=$(cat $HOME/opt/arthas/mybatisMapperXmlReloadResult.out | grep -E "@Boolean\[true\]")
  echo $mybatisMapperReloadCommandResult
  if [ -z "$mybatisMapperReloadCommandResult" ]; then
    banner_simple $(echo $(tput setaf 1)arthas idea plugin mybatis mapper xml reload error $(tput sgr0))
  else
    banner_simple "arthas idea plugin mybatis mapper xml reload success"
  fi
}

main() {

  banner_simple "arthas idea plugin hot swap begin;start script path: $(pwd)/arthas-idea-plugin-mybatis-mapper-xml-reload.sh"

  check_permission

  installArthas

  if [ $? -ne 0 ]; then
    exit_on_err 1 "arthas install as.sh script error"
  fi

  decodebase64MapperXmlFile
  if [ $? -ne 0 ]; then
    exit_on_err 1 "arthas idea plugin decodebase64MapperXmlFile error"
  fi

  if [ -z ${SELECT_VALUE} ]; then
    reset_for_env
    select_pid
    SELECT_VALUE=${TARGET_PID}
  fi

  if [ -z ${SELECT_VALUE} ]; then
    exit_on_err 1 "select target process by classname or JARfilename Target pid is empty"
  fi

  getSpringContextHashValue

  doStartMybatisMaperReloadCommand

  getMybatisMapperReloadCommandResult
}

main "${@}"
