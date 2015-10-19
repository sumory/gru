var zookeeper = require('node-zookeeper-client');
var config = require("../config");

var client = zookeeper.createClient(config.zk.addr);

client.once('connected', function () {
    console.log('Connected to the zookeeper server.');
});

client.connect();


exports.getSpears = function (callback) {
    var path = config.zk.spears;
    client.removeAllListeners();
    client.getChildren(path,
        function (error, children, stat) {
            if (error) {
                console.log(
                    'Failed to list children of %s due to: %s.',
                    path,
                    error
                );
                callback(error);
            }
            console.log('Children of %s are: %j.', path, children);
            callback(null, children);
        }
    );
}