TESTS = test/*.test.js
TIMEOUT = 200000
REPORTER = spec
NODE_ENV = dev

test:
	@NODE_ENV=${NODE_ENV} ./node_modules/mocha/bin/mocha \
		--timeout $(TIMEOUT) \
		--reporter ${REPORTER} \
		$(TESTS)


echo:
	echo "hello.."\
	 "this is Makefile"

.PHONY: test echo
