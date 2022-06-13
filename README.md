# Piyush's Task Engine Project

### How to Use

#### Build the Jar

From the project root, run:

```bash
./gradlew jar
```

This will output the jar in the `build/libs/` folder.
The default name is `task-engine-1.0-SNAPSHOT.jar`

#### Run the Jar

```bash
java -jar [path_to_jar] [path_to_action_file_json] [path_to_tasks_file_json]
```

Example:

```bash
java -jar build/libs/task-engine-1.0-SNAPSHOT.jar \
  "src/test/resources/sample_actions.json" \
  "src/test/resources/sample_tasks.json"
```

#### Run from code

```bash
./gradew run --args"[path_to_action_file_json] [path_to_tasks_file_json]"
```

Example:

```bash
./gradlew run  \
  --args="src/test/resources/sample_actions.json src/test/resources/sample_tasks.json"
```

### High Level Design

At a high level, my project works as so:

- Actions and Tasks are parsed from json files and converted as Java objects.
- Tasks are uploaded into DB (in memory map in this case).
- Actions are stored in a list in-memory.
- For each action:
    - Process it's function (E.g. Setting a field or creating new entity)
    - Check if any task instances are already associated with that entity and update them.
    - Check to see if any new tasks can be opened for that entity.
        - New task can be opened if an instance of that task is not already associated with that entity & all the
          triggerConditions are met.
    - For new tasks, create an instance of them, update them, and associate with entity.
    - After each action, all the current task instances are printed to console.
- Shutdown the db connection and close the application.

Please let me know if you have questions or issues running the application.