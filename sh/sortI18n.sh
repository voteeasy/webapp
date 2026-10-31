#!/bin/bash
echo "Sorting i18n files ...."

cd src/main/resources || exit
for file in voteeasy*.properties; do
  echo "Sorting $file"
  sort -o "$file" "$file"
done
cd - || exit
