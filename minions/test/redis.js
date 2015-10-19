var redis = require('redis');
var async = require('async');

function test() {
	var client = redis.createClient();
	client.on("error", function(err) {
		console.log("Error " + err);
	});

	client.on("ready", function() {

		var index = 0;
		var max = 1000000;
		var step = 30000;

		var interval = setInterval(function() {
			if (index >= max) {
				clearInterval(interval);
			}
			batchSome('test_key', index, index + step, max, function(c) {
				//console.log(c);
			});
			index = index + step;

		}, 100);
		//最大11897115，0到11897114
	});
}


process.on('uncaughtException', function(err) {
	console.log('Caught exception: ' + err);
});

function batchSome(key, start, stop, max, callback) {
	var count = 0;
	for (var i = start; i < stop; i++) {
		client.hset(key, i, i, function(err, result) {
			if (err) console.log(result);
			count++;
			if (count % 10000 == 0) {
				console.log('current count:', count, ' start:', start, ' stop:', stop, ' i:', i);
				callback(count);
			}
			if (count == max) {
				console.log('finish: ', count);
				console.timeEnd('init');
			}
		});
	}
}