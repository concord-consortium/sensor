#this should be turned into a plugin, or we should try to use 
#already existing nar plugin

cd $1
source properties

DEPLOY_URL=scpexe://source.concord.org/web/source.concord.org/html/software/maven2/internal/
DEPLOY_ID=cc-dist-repo-internal
CLASSIFIER=${PLATFORM}
NAR_FILE=${ARTIFACT_ID}-${PLATFORM}.nar

# need to have all the native files at the top direcotry
mkdir -p ../target/native-lib
cp ${NATIVE_FILES} ../target/native-lib/
cd ../target/native-lib
pwd
jar cf ../${NAR_FILE} *
cd -

mvn deploy:deploy-file -Dfile=../target/${NAR_FILE} \
    -DartifactId=${ARTIFACT_ID} -DgroupId=${GROUP_ID} \
    -Dclassifier=${CLASSIFIER} \
    -Dversion=${VERSION} -Dpackaging=nar \
    -Durl=${DEPLOY_URL} -DrepositoryId=${DEPLOY_ID}

cd -
