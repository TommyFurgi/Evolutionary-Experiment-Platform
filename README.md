
# 🏆 **Endpoint Explorers**

The application enables control of multi-objective evolutionary algorithms (MOEA) in a client-server architecture. The client is a console application that allows running experiments and collecting results from the server.

We are able to run experiments with specific problem names, algorithms, metrics and number of evaluations.

---

## Table of Contents

1. [Techologies used](#techologies-used)
2. [Model](#model)
3. [API Endpoints](#api-endpoints)
    - [Run Experiment](#run-experiment)
    - [Run Multiple Experiments](#run-multiple-experiments)
    - [Get Experiment By ID](#get-experiment-by-id)
    - [Get Ready Experiments](#get-ready-experiments)
    - [Get Experiments by various parameters](#get-experiments-by-filters)
    - [Set group name for experiments](#set-group-name-for-experiments)

4. [Cli Commands](#cli-commands)
    - [`run`](#run-command)
    - [`runMulti`](#runMulti-command)
    - [`get`](#get-command)
    - [`getStats`](#getStats-command)
    - [`list`](#list-command)
    - [`setGroup`](#setGroup-command)
    - [`exit`](#exit-command)
    - [`help`](#help-command)
5. [Running project](#running-project)
6. [Example usage](#example-usage)
7. [Authors](#authors)

---

## Techologies used

🌱 Spring Boot - server

🐘 Postgres - database

⚡ RxJava - running problems asynchronously

💻 PicoCLI - easy-to-use library for creating CLI

🔬 MOEA Framework - framework used for creating and running experiments

✂️ Lombok - automates the generation of boilerplate code like getters, 
setters, and constructors

🔗 Hibernate - handles object-relational mapping

🐳 Docker - containerization platform, used here for running Postgres

## Model


![Architecture Diagram](/docs/diagram.png "Architecture Overview")

### Experiment table

- **problem (String)**: Specifies the problem name. The complete list of problems can be found [here](https://github.com/MOEAFramework/MOEAFramework/blob/master/docs/listOfProblems.md).

- **algorithm (String)**: Specifies the algorithm name. The complete list of algorithms can be found [here](https://github.com/MOEAFramework/MOEAFramework/blob/master/docs/listOfAlgorithms.md).

- **numberOfEvaluations (Integer)**: Indicates how many iterations of the algorithm are performed. The greater the number, the longer the computations will take.

- **metricsList (List<Metrics>)**: For each experiment, we store multiple metrics. Metrics are recorded every 100th iteration. As a result, the total number of rows in the metrics table for a given experiment is calculated as:  
  **(Number of metrics provided × Number of evaluations) / 100**

- **status (Enum)**: Specifies the status of an experiment. The possible statuses are:  
  - **IN_PROGRESS**: The experiment is still running.  
  - **FAILED**: The experiment has failed.  
  - **READY**: The experiment is complete; all computations are done, but the CLI client has not yet been informed.  
  - **COMPLETED**: The experiment is finished, and the CLI client has been notified.

- **groupName (String)**: Specifies a user-defined identifier for grouping related experiments.

<span style="color: red;">**Important:**</span> CLI periodically sends requests to the endpoint `{BASE_URL}/experiment/ready`.  
If there is an experiment with status **READY**, the CLI is informed, and the status is changed to **COMPLETED** on the server side.


### Metrics table

- **metricsName (String)**: Specifies the name of the metric.

- **experiment (Experiment)**: Associates the metric with a specific experiment.

- **iterationNumber (Integer)**: Indicates the iteration during which the metric was computed. <span style="color: red;">Metrics are recorded every 100th iteration<span>

- **value (Float)**: Represents the value of the metric.


## API Endpoints

### Run Experiment
- **URL**: `/experiments`
- **Method**: `POST`
- **Description**: Starts a new experiment based on the provided configuration.

#### Request Body:
The endpoint accepts a JSON object that must match the `RunExperimentRequest` structure. The fields are described below:

- **`problemName`** (String): The name of the problem to solve.  
  **Constraints**:  
  - Cannot be blank.

- **`algorithm`** (String): The algorithm to use for solving the problem.  
  **Constraints**:  
  - Cannot be blank.

- **`metrics`** (List of Strings): A list of metrics to evaluate during the experiment.  
  **Constraints**:  
  - Must include at least one metric.  
  **Example**: `["contribution", "spacing"]`

- **`evaluationNumber`** (Integer): The number of evaluations for the experiment.  
  **Constraints**:  
  - Must be greater than 0.  

- **`experimentIterationNumber`** (Integer): The number of iterations for the experiment.  
  **Constraints**:  
  - Must be greater than 0.  

- **`groupName`** (String): Name of the group.
  **Example**: `"group1"`

#### Example Request:
```json
{
  "problemName": "UF1",
  "algorithm": "NSGA-II",
  "metrics": ["contribution", "spacing"],
  "evaluationNumber": 1000,
  "experimentIterationNumber": 1,
  "groupName": "group1"
}
```


---

### Run Many Different Experiments
- **URL**: `/experiments/manyDifferent`
- **Method**: `POST`
- **Description**: Triggers multiple experiments for combinations of problems and algorithms. Each experiment is executed based on the parameters provided.

#### Request Body:
The endpoint accepts a JSON object with the following required fields:

- **`problems`** (List of Strings): A list of problems to solve.  
  **Example**: `["UF1", "DTLZ2"]`

- **`algorithms`** (List of Strings): A list of algorithms to apply for solving the problems.  
  **Example**: `["NSGA-II", "GDE3"]`

- **`metrics`** (List of Strings): A list of metrics to evaluate during the experiments.  
  **Example**: `["contribution", "spacing"]`

- **`evaluationNumber`** (Integer): The number of evaluations to perform for each experiment.  
  **Minimum Value**: `1`  
  **Default**: No default; must be explicitly provided.

- **`experimentIterationNumber`** (Integer): The number of iterations for each experiment.  
  **Minimum Value**: `1`  
  **Default**: No default; must be explicitly provided.

- **`groupName`** (String): Name of the group.
  **Example**: `"group1"`

#### Example Request:
```json
{
  "problems": ["UF1", "DTLZ2"],
  "algorithms": ["NSGA-II", "GDE3"],
  "metrics": ["enlapsed-time", "spacing"],
  "evaluationNumber": 1000,
  "experimentIterationNumber": 2,
  "groupName": "group1"
}
```
---

### Get Experiment By ID
- **URL**: `/experiments/{id}`
- **Method**: `GET`
- **Description**: Fetches details of a specific experiment by its ID.
- **Path Parameter**:
  - `id` (Integer): The ID of the experiment.

---

### Get Ready Experiments
- **URL**: `/experiments/ready`
- **Method**: `GET`
- **Description**: Retrieves a list of experiments that are marked as **READY** (completed computations, but not yet acknowledged by the CLI).

---

### Get Experiments By Filters
- **URL**: `/experiment-list`
- **Method**: `POST`
- **Description**: Retrieves a list of experiments filtered by various criteria, including status, problem name, algorithm, and metrics.

#### Request Body:
The endpoint accepts a JSON object with the following optional keys:

- **`statuses`** (List of Strings): Filter experiments by their statuses. Valid statuses include:
  - `IN_PROGRESS`
  - `READY`
  - `FAILED`
  - `COMPLETED`

- **`problems`** (List of Strings): Filter experiments by their problem names.  
  **Example**: `["UF1", "DTLZ2"]`

- **`algorithms`** (List of Strings): Filter experiments by the algorithms used.  
  **Example**: `["NSGA-II", "GDE3"]`

- **`metrics`** (List of Strings): Filter experiments by the metrics computed.  
  **Example**: `["elapsed-time", "spacing"]`

- **`groupNames`** (List of Strings): Filter experiments by the group it belongs to.
  **Example**: `"group1"`

#### Example Request:
```json
{
  "statuses": ["READY", "COMPLETED"],
  "problems": ["UF1"],
  "algorithms": ["NSGA-II"],
  "metrics": ["spacing"],
  "groupNames": ["group1", "group2"]
}
```

---

### Set group name for experiments
- **URL**: `/experiments/group`
- **Method**: `PUT`
- **Description**: Sets the group for one or more experiments.

#### Request Body:
The endpoint accepts a JSON object with the following required fields:

- **`experimentIds`** (List of Integer): A list of experiment IDs that need to be updated.  
  **Example**: `[1, 2, 3]`

- **`groupName`** (String): The new group name to assign to the experiments.  
  **Example**: `"newGroup"`

#### Example Request:
```json
{
  "experimentIds": [1, 2, 3],
  "groupName": "newGroup"
}
```

---


## Cli Commands

### `run` command
**Description**: Run an experiment or multiple iterations of an experiment on the server.

#### Usage
```bash
run <problemName> <algorithm> [options]
```

#### Parameters
`<problemName>`: The name of the problem to solve.
`<algorithm>`: The algorithm to use for solving the problem.

##### Options
`-m, --metrics <metric1> <metric2> ...`: A list of metrics to evaluate. Defaults to all if not specified. Example: -m hypervolume spacing

`-e, --evaluations <number>`: The number of evaluations to perform. Defaults to 1000. Example: -e 5000

`-n, --experimentIterationNumber <number>`: The number of iterations for the experiment. Defaults to 1. Example: -n 10

`-g, --groupName <group1>`: Name of the group. Defaults to "". Example: -g group1


---


### `runMulti` command
**Description**: Run multiple experiments on the server, each defined by a list of problems and a list of algorithms. You can also specify metrics, number of evaluations, and how many times to repeat each experiment.

#### Usage
```bash
runMulti [options]
```

#### Required Options
`-p, --problems <problem1> <problem2> ...`

The list of problem names to solve (at least one).
Example: -p UF1 DTLZ2

`-a, --algorithms <algorithm1> <algorithm2> ...`

The list of algorithms to use (at least one).
Example: -a e-MOEA NSGA-II

#### Optional Options
`-m, --metrics <metric1> <metric2> ...`
The list of metrics to evaluate for each experiment (default: all).
Example: -m hypervolume spacing

`-e, --evaluations <number>`
The number of evaluations to perform in each experiment (default: 1000).
Example: -e 5000

`-n, --experimentIterationNumber <number>`
How many times each (problem, algorithm) pair should be repeated (default: 1).
Example: -n 3

`-g, --groupName <group1>`: 
Name of the group (default: ""). 
Example: -g group1


#### Parameters

- `<problemName>`: The name of the problem to solve.
- `<algorithm>`: The algorithm to use for solving the problem.

#### Options

- `-m, --metrics <metric1> <metric2> ...`: List of metrics to evaluate (default: `all`).
- `-e, --evaluations <number>`: The number of evaluations to perform (default: `1000`).
- `-g, --groupName <group1>`: Name of the group (default: `""`).

---

### `get` command
**Description**: Retrieve details of a specific experiment from the server.

#### Usage
```bash
get <experimentId>
```

#### Parameters

- `<experimentId>`: The id of the experiment.

---


### `getStats` command
**Description**: Fetch experiment statistics from the server for a given problem, algorithm, and time interval. The statistics can include metrics like `median`, `average`, etc., calculated over the specified interval.

#### Usage
```bash
getStats <problemName> <algorithm> [options]
[options]
```

#### Parameters
`<problemName>`: The name of the problem for which stats are being calculated.
Example: UF1

`<algorithm>`: The algorithm used for solving the problem.
Example: NSGA-II


#### Options
`-s, --start <startDateTime>`
Start of the time interval
Format: yyyy-MM-dd_HH:mm:ss
Default: 2024-01-01_00:00:00

Example
--start 2024-01-01_00:00:00

`-e, --end <endDateTime>`
End of the time interval

Format: yyyy-MM-dd_HH:mm:ss
Default (if not provided): current time and date

Example:
--end 2024-01-02_17:20:00

`-a, --statType <type>`
Statistics type
Available options: `median, avg, std_dev`.
Default: median.

`-g, --groupName <group1>`:
Name of the group
Default: "".

Example:
`--statType avg`

---
### `list` command
**Description**: Retrieve information about experiments filtered by various parameters such as status, problem name, algorithm, or metrics.


#### Usage
```bash
list [options]
```
#### Options
`-s, --status <status1> <status2> ...`: Filter experiments by their status. Valid statuses include:
IN_PROGRESS, READY, FAILED, COMPLETED

Defaults to all statuses if not specified.
`-p, --problem <problem1> <problem2> ...`: Filter experiments by their problem name(s).
Example: --problem UF1 DTLZ2

`-a, --algorithm <algorithm1> <algorithm2> ...`: Filter experiments by their algorithm(s).
Example: --algorithm NSGA-II GDE3

`-m, --metrics <metric1> <metric2> ...`: Filter experiments by their metrics.
Example: --metrics spacing

`-g, --groupNames <group1> <group2> ...`: Filter experiments by the group it belongs to.
Example: --groupNames group1

---
### `setGroup` command
**Description**: Sets or updates the group for one or more experiments.


#### Usage
```bash
setGroup <experimentId1> <experimentId2> ... -g <groupName>
```

#### Parameters
`<experimentId1> <experimentId2>`: A list of experiment IDs to be assigned to the specified group.
Example: 1 2 3

#### Options
`-g --groupName newName`: The name of the group to which the experiments will be assigned.  
Example: --groupName newGroup

---
### `exit` command
**Description**: Exits the CLI application.

#### Usage
```bash
exit
```
---

### `help` command
**Description**: Displays a list of available commands with descriptions or provides details for a specific command.

#### Usage
```bash
help [command]
```
or we can list available commands:

```bash
help
```
#### Parameters

- `[command]`: one of the avaiable commands in application for example `run` or `get`



---





## Running Project
### Prerequisites

1. **Clone the Project**  
   Clone the repository to your local machine.

2. **Java 21**  
   Ensure you have Java 21 installed (the project is developed with Java 21).

3. **Docker Engine**  
   Ensure Docker Engine is running in the background. It is necessary for setting up the PostgreSQL database.


### Setup Instructions

1. **Run**`docker-compose.yml`  
   Execute the `docker-compose.yml` file to set up the database.  

   - The credentials for connecting to the database are included in the `docker-compose.yml` file.
   - Pay attention to the `POSTGRES_DB: database` setting for the database name.

2. **Start the Server**  
   Run the `EndpointExplorersApplication` class to start the server.

3. **Run the CLI Application**  
   Open a second terminal and run the `InteractiveApp` class to launch the CLI.


##### Everything Should Work!

---

## Example usage


**By default, experiments are run with all metrics attached and 1,000 evaluations. For example, to run the UF1 problem with the e-MOEA algorithm:**


```bash
run UF1 e-MOEA 
```

**After starting an experiment, you receive an ID and a message indicating whether the experiment is completed. If the experiment is completed, you can fetch the results using the ID:**

```bash
get 1 # if the ID of the experiment is 1
```
**You can specify the number of evaluations for an experiment. For instance, to run the DTLZ2 problem with the GDE3 algorithm and 10,000 evaluations:**

```bash
run DTLZ2 GDE3 -e 10000
```

**You can also specify the metrics to compute during the experiment. For example:**

```bash
run DTLZ2 GDE3 -m generational-distance -e 10000
```


**You can pass multiple metrics separated by spaces:**

```bash
run WFG8 e-NSGA-II -m spacing population-size additive-epsilon-indicator
```

**To try something heavy, you can run the same experiment multiple times. For example, run the following command 8 times:**

```bash
run UF1 e-MOEA -e 20000
```
**You can also specify the number of instances for the experiment by adding the `-n {number of instances}` option.**

```bash
run UF1 e-MOEA -n 10
```
**Examples for the runMulti Command:**

**Run multiple experiments with multiple problems and algorithms:**

```bash
runMulti -p UF1 DTLZ2 -a e-MOEA NSGA-II -e 5000 -n 3
```

**You can check the statistics of completed experiments:**
```bash
getStats UF1 e-MOEA
```

```bash
getStats UF1 e-MOEA -a std_dev
```

**To specify a start date for the included experiments: (default 2024-01-01_00:00:00)**

```bash
getStats UF1 e-MOEA -a std_dev --start 2025-01-01_12:00:00
```
**You can also specify an end date (by default, it's set to the current date and time):**

```bash
getStats UF1 e-MOEA -a std_dev --start 2025-01-01_12:00:00 --end 2026-01-01_12:00:00
```


**Now try to see that they are running in the background - list the status of all experiments, use the list command:**

```bash 
list
```
**Filter experiments by their statuses (e.g., READY and COMPLETED):**

```bash 
list -s READY COMPLETED
```

**Filter experiments by specific problems and algorithms:**

```bash
list -p UF1 DTLZ2 -a NSGA-II e-MOEA
```

**Retrieve experiments with specific metrics:**

```bash
list -m spacing contribution elapsed-time
```
**You can combine all**

```bash
list -s COMPLETED -p UF1 -a NSGA-II -m spacing
```


**You can try other problems**
- from ZDT1 to  ZDT9
- from DTLZ1 to DTLZ7
- from LZ1 to LZ9
- from CF1 to CF10
- from UF1 to UF13
- from WFG1 to WFG9

**Here are some other algorithms**

- AGE-MOEA-II
- AMOSA
- DBEA
- e-NSGA-II
- GDE3
- MSOPS
- NSGA-II

**Here are all the metrics**
<p style="color:red"><strong>!important</strong> Sometimes we can't compute certain metrics without others.</p>

- elapsed-time
- additive-epsilon-indicator
- archive-size
- contribution
- generational-distance
- generational-distance-plus
- hypervolume
- inverted-generational-distance
- inverted-generational-distance-plus
- maximum-pareto-front-error
- number-of-dominating-improvements
- number-of-improvements
- population-size
- r1-indicator
- r2-indicator
- r3-indicator
- spacing

## Authors

- **Wiktor Dybalski**  
- **Tomasz Furgała**  
- **Piotr Śmiałek**  


