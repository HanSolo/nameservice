#!/bin/bash

JAVA_VERSION=17
MAIN_CLASS="eu.hansolo.springboot.SpringbootApplication"
MAIN_JAR="nameservice-17.0.0.jar"
BUILD_TOOL="gradle" # either gradle or maven

echo "java home       : $JAVA_HOME"
echo "main class      : $MAIN_CLASS"
echo "main JAR file   : $MAIN_JAR"

if [ "$BUILD_TOOL" = "gradle" ]; then
  echo "build tool      : $BUILD_TOOL"
elif [ "$BUILD_TOOL" = "maven" ]; then
  echo "build tool      : $BUILD_TOOL"
else
  echo "Build tool need to be either 'gradle' or 'maven'"
  exit 1
fi

# ------ SETUP DIRECTORIES AND FILES ------------------------------------------
# Remove previously generated java runtime and installers. Copy all required
# jar files into the input/libs folder.

if [ "$BUILD_TOOL" = "gradle" ]; then
  ## Gradle
  rm -rfd ./build/jlink-runtime/
  rm -rfd build/installer/

  mkdir -p build/installer/input/libs/

  cp build/libs/* build/installer/input/libs/
  cp build/libs/${MAIN_JAR} build/installer/input/libs/
elif [ "$BUILD_TOOL" = "maven" ]; then
  ## Maven
  rm -rfd ./target/jlink-runtime/
  rm -rfd target/installer/

  mkdir -p target/installer/input/libs/

  cp target/* target/installer/input/libs/
  cp target/${MAIN_JAR} target/installer/input/libs/
fi

# ------ REQUIRED MODULES -----------------------------------------------------
# Use jlink to detect all modules that are required to run the application.
# Starting point for the jdep analysis is the set of jars being used by the
# application.

#echo "detecting required modules"
#MAIN_CLASS_PATH="${MAIN_CLASS//.//}"
#detected_modules=`$JAVA_HOME/bin/jdeps \
#  --multi-release ${JAVA_VERSION} \
#  --ignore-missing-deps \
#  --print-module-deps \
#  --class-path "build/installer/input/libs/*" \
#    build/classes/java/main/${MAIN_CLASS_PATH}.class`
#echo "detected modules: ${detected_modules}"

if [ "$BUILD_TOOL" = "gradle" ]; then
  detected_modules=`$JAVA_HOME/bin/jdeps \
    --multi-release ${JAVA_VERSION} \
    --ignore-missing-deps \
    --print-module-deps \
    --class-path "libs/* recursive" \
    build/libs/${MAIN_JAR}`
  echo "detected modules: ${detected_modules}"
elif [ "$BUILD_TOOL" = "maven" ]; then
  detected_modules=`$JAVA_HOME/bin/jdeps \
      --multi-release ${JAVA_VERSION} \
      --ignore-missing-deps \
      --print-module-deps \
      --class-path "libs/* recursive" \
      target/${MAIN_JAR}`
    echo "detected modules: ${detected_modules}"
fi

# ------ MANUAL MODULES -------------------------------------------------------
# jdk.crypto.ec has to be added manually bound via --bind-services or
# otherwise HTTPS does not work.
#
# See: https://bugs.openjdk.java.net/browse/JDK-8221674
#
# In addition we need jdk.localedata if the application is localized.
# This can be reduced to the actually needed locales via a jlink paramter,
# e.g., --include-locales=en,de.

manual_modules=jdk.crypto.ec,jdk.localedata
echo "manual modules  : ${manual_modules}"


# ------ RUNTIME IMAGE --------------------------------------------------------
# Use the jlink tool to create a runtime image for our application. We are
# doing this is a separate step instead of letting jlink do the work as part
# of the jpackage tool. This approach allows for finer configuration and also
# works with dependencies that are not fully modularized, yet.

#echo "creating java runtime image"
if [ "$BUILD_TOOL" = "gradle" ]; then
  $JAVA_HOME/bin/jlink \
    --no-header-files \
    --no-man-pages  \
    --compress=2  \
    --strip-debug \
    --add-modules "${detected_modules},${manual_modules}" \
    --include-locales=en,de \
    --output build/jlink-runtime

  runtimeSize=`du -sh build/jlink-runtime`

  echo "Successfully created custom Java Runtime Image: ${runtimeSize}"
elif [ "$BUILD_TOOL" = "maven" ]; then
  $JAVA_HOME/bin/jlink \
      --no-header-files \
      --no-man-pages  \
      --compress=2  \
      --strip-debug \
      --add-modules "${detected_modules},${manual_modules}" \
      --include-locales=en,de \
      --output target/jlink-runtime

    runtimeSize=`du -sh target/jlink-runtime`

    echo "Successfully created custom Java Runtime Image: ${runtimeSize}"
fi
