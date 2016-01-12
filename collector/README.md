
# Setup

Ensure you have `easy_install`, `pip` and `virtualenv` installed at a system level.

Ensure you have `make` on your system.

Ensure you have AWS credentials on your system for the `frontend` account.

## Python Environment

After checking out the repo, change your directory to `collector` to change to the python app.

To setup a local python environment in this context:


```bash
virtualenv .

```

Then change into the context of the environment with:

```bash
source bin/activate
```

### Python Dependencies

Now you need to install the python dependencies.

```bash
pip install -r requirements.txt
```

Note that one of the dependencies is a modified `FuelSDK-Python` client with better complex queries and more types, this new version does NOT end up in your local `site-packages` and instead ends up under `src/`.

### Make runnable lambda function

Assuming you have `make` on your system, run

```bash
make build
```

This will now create a `dist/` directory with the exact structure of how the lambda function should be laid out, also in here is the `function.zip` file that can be used to upload to lambda.

