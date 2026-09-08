#!/bin/bash
# elasticsearch/init/create-playlist-index.sh
# Elasticsearch playlist 인덱스 생성 스크립트
# 최초 1회 수동 실행 필요 (MySQL FULLTEXT 인덱스와 동일한 관리 방식)
# 실행 명령어 (우분투 기준)
# chmod +x elasticsearch/init/create-playlist-index.sh ./elasticsearch/init/create-playlist-index.sh
#
# curl http://localhost:9200/playlist/_mapping



ES_HOST="${ES_HOST:-http://localhost:9200}"

curl -X PUT "${ES_HOST}/playlist" \
  -H "Content-Type: application/json" \
  -d '{
    "settings": {
      "analysis": {
        "analyzer": {
          "korean_analyzer": {
            "type": "custom",
            "tokenizer": "nori_tokenizer",
            "filter": ["nori_part_of_speech", "lowercase"]
          }
        }
      }
    },
    "mappings": {
      "properties": {
        "playlistId": { "type": "keyword" },
        "title": {
          "type": "text",
          "analyzer": "korean_analyzer"
        },
        "comment": {
          "type": "text",
          "analyzer": "korean_analyzer"
        },
        "searchText": {
          "type": "text",
          "analyzer": "korean_analyzer"
        },
        "nickname": { "type": "keyword" },
        "memberId": { "type": "keyword" },
        "registerDate": {
          "type": "date",
          "format": "date_hour_minute_second"
        }
      }
    }
  }'
