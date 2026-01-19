#!/bin/bash
docker run --rm -v $(pwd):/base kubling/kubling-cli:latest bundle genmod /base/descriptor -o /base/orders-descriptor-bundle.zip --parse
docker run --rm -v $(pwd):/base kubling/kubling-cli:latest bundle genmod /base/mod-apis -o /base/mod-apis-bundle.zip --parse