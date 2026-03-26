# JMeter Load Testing

This folder contains the JMeter part of the lab. The race-condition demonstration remains available through the demo controller, while this folder covers the load-testing requirement for the real REST API.

## Files
- `taskmanager-load-test.jmx` - JMeter test plan
- `demo-race-condition-test.jmx` - JMeter plan for `POST /api/demo/race-condition`
- `report-template.md` - short template for the submitted conclusion

## Demo controller: how to show that the counter converges
Use `POST /api/demo/race-condition` for the race-condition part of the lab.

What to show in the response:
- `threadCount = 50`
- `incrementsPerThread = 200`
- expected final value is `50 * 200 = 10000`
- `unsafeCounter.actualValue < 10000` means lost updates happened
- `synchronizedCounter.actualValue = 10000` and `correct = true`
- `atomicCounter.actualValue = 10000` and `correct = true`

In other words, the fixed counter "converges" when `actualValue` matches `expectedValue`
and `lostUpdates = 0`.

Example:
```json
{
  "threadCount": 50,
  "incrementsPerThread": 200,
  "unsafeCounter": {
    "counterName": "Unsafe counter",
    "expectedValue": 10000,
    "actualValue": 7348,
    "lostUpdates": 2652,
    "correct": false
  },
  "synchronizedCounter": {
    "counterName": "Synchronized counter",
    "expectedValue": 10000,
    "actualValue": 10000,
    "lostUpdates": 0,
    "correct": true
  },
  "atomicCounter": {
    "counterName": "Atomic counter",
    "expectedValue": 10000,
    "actualValue": 10000,
    "lostUpdates": 0,
    "correct": true
  }
}
```

If you want to demonstrate this under external concurrent load in JMeter, send parallel
requests to `POST /api/demo/race-condition` and check that each response still has:
- `synchronizedCounter.correct = true`
- `atomicCounter.correct = true`

## Demo race-condition plan in JMeter
Open `docs/jmeter/demo-race-condition-test.jmx` when you need to demonstrate the
`DemoController` endpoint itself.

Default setup in this plan:
- `users=10`
- `ramp_up_seconds=1`
- `loops=5`
- `response_timeout_ms=15000`
- sampler: `POST /api/demo/race-condition`

What this plan already checks:
- response contains `"threadCount":50,"incrementsPerThread":200`
- `synchronizedCounter` returned `actualValue = 10000`
- `atomicCounter` returned `actualValue = 10000`

For a live demo:
1. Open the plan in JMeter.
2. Run it once with `View Results Tree` enabled.
3. Open any sample and show the JSON body.
4. Point out that `unsafeCounter.actualValue` is below `10000`, while
   `synchronizedCounter` and `atomicCounter` are exactly `10000`.
5. Show `Summary Report` to confirm that all assertions passed under parallel load.

The demo endpoint can take a little over 5 seconds when several JMeter users call it at once,
because each request launches its own internal 50-thread race-condition simulation. The demo
plan therefore uses a higher default `response_timeout_ms=15000` to avoid false failures caused
by JMeter timing out before the JSON response arrives.

## Demo plan from CLI
From the project root:

```bash
./apache-jmeter-5.6.3/bin/jmeter -n \
  -t docs/jmeter/demo-race-condition-test.jmx \
  -l docs/jmeter/demo-results.jtl \
  -e -o docs/jmeter/demo-report
```

If you are already inside `apache-jmeter-5.6.3/bin`, then `./jmeter` also works, but the paths
to the `.jmx`, `.jtl`, and report folder must stay relative to that `bin` directory:

```bash
./jmeter -n \
  -t ../../docs/jmeter/demo-race-condition-test.jmx \
  -l ../../docs/jmeter/demo-results.jtl \
  -e -o ../../docs/jmeter/demo-report
```

To override the timeout from CLI, add for example:

```bash
-Jresponse_timeout_ms=15000
```

## Covered scenario
The plan creates reference entities once per virtual user:
- `POST /api/users`
- `POST /api/tags`
- `POST /api/projects`

Then each loop executes a task lifecycle:
- `POST /api/tasks`
- `GET /api/tasks/{id}`
- `GET /api/tasks`
- `GET /api/tasks/with-tags`
- `GET /api/tasks/search/by-project-owner`
- `GET /api/tasks/search/by-tag/native`
- `PUT /api/tasks/{id}`
- `POST /api/comments`
- `GET /api/comments`

## Default parameters
- `protocol=http`
- `host=localhost`
- `port=8080`
- `users=20`
- `ramp_up_seconds=10`
- `loops=5`
- `connect_timeout_ms=5000`
- `response_timeout_ms=5000`
- `page_size=10`

## Run in GUI
1. Start the application.
2. Open Apache JMeter.
3. Load `docs/jmeter/taskmanager-load-test.jmx`.
4. Adjust variables in `Test Plan -> User Defined Variables` if needed.
5. Run the test.

GUI mode needs a desktop session with an X11/Wayland display. In a headless terminal you will see
`No X11 DISPLAY variable was set`, so use CLI mode there instead.

## Run in CLI
```bash
./apache-jmeter-5.6.3/bin/jmeter -n \
  -t docs/jmeter/taskmanager-load-test.jmx \
  -l docs/jmeter/results.jtl \
  -e -o docs/jmeter/report
```

## Override parameters from CLI
```bash
./apache-jmeter-5.6.3/bin/jmeter -n \
  -Jhost=localhost \
  -Jport=8080 \
  -Jusers=50 \
  -Jloops=10 \
  -t docs/jmeter/taskmanager-load-test.jmx \
  -l docs/jmeter/results.jtl \
  -e -o docs/jmeter/report
```

## What to include in the submission
- Screenshot of the thread group and key samplers in JMeter
- HTML report from `docs/jmeter/report`
- Short conclusion with throughput, average response time, and error rate
