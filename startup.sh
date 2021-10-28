#!/usr/bin/env zsh
# shellcheck disable=SC2164
# 起動する前に以前のbootstrap.jarを削除
rm -rf bootstrap.jar
# jarファイルを作成，out/production/diytomcatのBootstrapクラスとCommonClassLoaderクラスをbootstrap.jarに入れる。この二つのクラスのクラスローダーはAppClassLoaderで，他のクラスは全部CommonClassLoaderによってロードされる。
# c：jarファイルを生成　v：JARファイルがビルドされている間、標準出力に詳細な出力を行う
# f：アウトプットをファイルに出力する
# 0：圧縮しない
jar cvf0 bootstrap.jar -C out/production/diytomcat com/zzy/diytomcat/Bootstrap.class -C out/production/diytomcat com/zzy/diytomcat/classloader/CommonClassLoader.class
# diytomcat.jarも削除する
rm -rf lib/diytomcat.jar
cd out
cd production
cd diytomcat
# /out/production/tomcat　中のファイルを使ってjarファイルを作る
jar cvf0 ../../../lib/diytomcat.jar *
# shellcheck disable=SC2103
cd ..
cd ..
cd ..
# 起動
java -cp bootstrap.jar com/zzy/diytomcat/Bootstrap