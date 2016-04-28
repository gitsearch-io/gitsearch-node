# gitsearch-node
This is a services that clones public git repositories and index the source code from all branches in elasticsearch
to make it searchable.
After the repository is cloned the service can perform a git pull and reindex changed files and index new files.

## Dependencies
- [Elasticsearch](https://www.elastic.co/)
- AMQP 0-9-1 Message broker (e.g. [RabbitMQ](https://www.rabbitmq.com))
- [Mongodb](https://www.mongodb.org/)

## Start
mvn package exec:java

## Usage
The service can be triggered by sending a message to two different queues. Both queues expect a message with
a https url address as payload e.g. `https://github.com/gitsearch-io/gitsearch-node.git`

#### `CLONE` queue
Clones the repository in the message

#### `UPDATE` queue
Update the repository in the message. It is a requirement that the repository has been cloned before it can be updated

## Test
mvn verify
