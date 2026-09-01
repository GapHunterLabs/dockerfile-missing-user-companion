# Flagged: this stage never sets a non-root USER -- the final CMD
# runs as root by default.
FROM node:20-slim

WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .

CMD ["node", "index.js"]
