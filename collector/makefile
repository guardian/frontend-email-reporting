

make-empty-dist:
	if [ -d "dist" ]; \
	then \
		rm -Rf dist ; \
	fi;
	mkdir dist

copy-site-packages:
	cp -R ./lib/python2.7/site-packages/* dist/

copy-custom-fuel-sdk:
	cp -R src/fuelsdk/FuelSDK dist/

copy-all-python:
	cp -R ./*.py dist/

make-lambda-zip:
	cd dist/ ; \
	zip -r function ./

build: make-empty-dist copy-site-packages copy-custom-fuel-sdk copy-all-python make-lambda-zip
