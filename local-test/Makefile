node1:
    redis-server ./redis_1.conf
node2:
    redis-server ./redis_2.conf
node3:
    redis-server ./redis_3.conf
create-cluster:
    redis-cli --cluster create 127.0.0.1:45401 127.0.0.1:45402 127.0.0.1:45403
