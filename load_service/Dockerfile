FROM grafana/k6:latest

WORKDIR /scripts
COPY load.js .

ENTRYPOINT ["k6", "run", "/scripts/load.js"]