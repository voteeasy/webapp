#!/bin/bash
echo "Generating Maven site...."
./mvnw -Pdevelopment clean clover:instrument install
./mvnw site:site
open ./target/site/index.html
