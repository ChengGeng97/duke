public class UserInputProcessor {
    //returns a DukeReply to be printed out, or a throws an error if input cannot be parsed by Duke
    public static DukeReply processUserInput(String userInputString, TaskList tasks) throws DukeException {
        switch(identifyUserInputType(userInputString)){
        case Bye: //Duke will close down
            return processByeCase();
            //Fallthrough
        case List: //Duke will return the full list of Tasks
            return processListCase(tasks);
            //Fallthrough
        case Done: //Duke will mark one Task for completion
            return processDoneCase(userInputString, tasks);
            //Fallthrough
        case Delete:
            return processDeleteCase(userInputString, tasks);            
            //Fallthrough
        case Nuke:
            return processNukeCase(userInputString, tasks);
        case ToDo: //Duke will record a ToDo Task
            return processToDoCase(userInputString, tasks);
            //Fallthrough
        case Deadline: //Duke will record a Deadline Task
            return processDeadlineCase(userInputString, tasks);
            //Fallthrough
        case Event: //Duke will record an Event Task
            return processEventCase(userInputString, tasks);
            //Fallthrough
        case Invalid: //Something went wrong
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_UNDECIPHERABLE_MESSAGE));
            //Fallthrough
        default: //Something went wrong
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_UNDECIPHERABLE_MESSAGE));
            //Fallthrough
        }
    }

    //Exists to make processUserInput a lot neater, by identifying the type of command the User issued.
    private static userInputType identifyUserInputType(String userInputString) {
        if(userInputString.toLowerCase().startsWith("bye")) {
            return userInputType.Bye;
        } else if (userInputString.toLowerCase().startsWith("list")) {
            return userInputType.List;
        } else if (userInputString.toLowerCase().startsWith("done")) {
            return userInputType.Done;
        } else if (userInputString.toLowerCase().startsWith("delete")) {
            return userInputType.Delete;
        } else if (userInputString.toLowerCase().startsWith("nuke")) {
            return userInputType.Nuke;
        } else if (userInputString.toLowerCase().startsWith("todo")) {
            return userInputType.ToDo;
        } else if (userInputString.toLowerCase().startsWith("deadline")) {
            return userInputType.Deadline;
        } else if (userInputString.toLowerCase().startsWith("event")) {
            return userInputType.Event;
        } else {
            return userInputType.Invalid;
        }
    }

    //Used to identify the type of command issued by the User
    private static enum userInputType {
        Bye, List, Done, Delete, Nuke, ToDo, Deadline, Event, Invalid
    };

    private static DukeReply processByeCase () {
        String dukeFeedback = DukeTextFormatter.makeFormattedText(DukeUi.GREET_BYE);
        return new DukeReply(true, false, dukeFeedback);
    }

    private static DukeReply processListCase(TaskList tasks) {
        if (tasks.isEmpty()) {
            String dukeFeedback = DukeTextFormatter.makeFormattedText(DukeUi.FEEDBACK_EMPTY_LIST);
            return new DukeReply(false, false, dukeFeedback);
        } else {
            String dukeFeedback = DukeTextFormatter.makeFormattedText(tasks.toString());
            return new DukeReply(false, false, dukeFeedback);
        }
    }

    private static DukeReply processDoneCase(String userInputString, TaskList tasks) throws DukeException {
        String indexString = "";

        try{
            String [] splitString = userInputString.split(" ");

            if(splitString.length == 1) {
                String exceptionMessage 
                    = DukeTextFormatter.makeFormattedText(String.format(DukeUi.ERROR_INCOMPLETE_COMMAND, "done"));
                throw new DukeException(exceptionMessage);
            }

            indexString = splitString[1];
            int userSpecifiedIndex = Integer.parseInt(splitString[1]);
            Task newlyFinishedTask = tasks.markAsDone(userSpecifiedIndex);

            String dukeFeedback 
                = DukeTextFormatter.makeFormattedText(String.format(DukeUi.FEEDBACK_TASK_DONE,
                                                                    newlyFinishedTask.toString(),
                                                                    tasks.size()));
            return new DukeReply(false, true, dukeFeedback); 
        } catch (NumberFormatException e) {
            String exceptionMessage 
                = DukeTextFormatter.makeFormattedText(String.format(DukeUi.ERROR_NOT_NUMBER, indexString));
            throw new DukeException(exceptionMessage);
        }
        
    }

    private static DukeReply processDeleteCase(String userInputString, TaskList tasks) throws DukeException {
        String indexString = "";

        try{
            String [] splitString = userInputString.split(" ");

            if(splitString.length == 1) {
                String exceptionMessage 
                    = DukeTextFormatter.makeFormattedText(String.format(DukeUi.ERROR_INCOMPLETE_COMMAND, "done"));
                throw new DukeException(exceptionMessage);
            }

            indexString = splitString[1];
            int userSpecifiedIndex = Integer.parseInt(splitString[1]);
            Task newlyDeletedTask = tasks.deleteAt(userSpecifiedIndex);
            
            String dukeFeedback 
                = DukeTextFormatter.makeFormattedText(String.format(DukeUi.FEEDBACK_TASK_DELETE,
                                                                    newlyDeletedTask.toString(),
                                                                    tasks.size()));
            return new DukeReply(false, true, dukeFeedback);         
        } catch(NumberFormatException e) {
            String exceptionMessage 
                = DukeTextFormatter.makeFormattedText(String.format(DukeUi.ERROR_NOT_NUMBER, indexString));
            throw new DukeException(exceptionMessage);
        }
    }

    private static DukeReply processNukeCase(String userInputString, TaskList tasks) {
        tasks.deleteAllTasks();
        String dukeFeedback = DukeTextFormatter.makeFormattedText(DukeUi.FEEDBACK_NUKE);
        return new DukeReply(false, true, dukeFeedback);
    }

    private static DukeReply processToDoCase(String userInputString, TaskList tasks) throws DukeException {
        Task newlyAddedTask = TextToTaskTranslator.translateToDoTask(userInputString);
        tasks.add(newlyAddedTask);

        String dukeFeedback = DukeTextFormatter.makeFormattedText(String.format(DukeUi.FEEDBACK_TASK_ADDED,
                                                                                newlyAddedTask.toString(),
                                                                                tasks.size()));
        return new DukeReply(false, true, dukeFeedback);
    }

    private static DukeReply processDeadlineCase(String userInputString, TaskList tasks) throws DukeException {
        Task newlyAddedTask = TextToTaskTranslator.translateDeadlineTask(userInputString);
        tasks.add(newlyAddedTask);

        String dukeFeedback = DukeTextFormatter.makeFormattedText(String.format(DukeUi.FEEDBACK_TASK_ADDED,
                                                                                newlyAddedTask.toString(),
                                                                                tasks.size()));
        return new DukeReply(false, true, dukeFeedback);
    }

    private static DukeReply processEventCase(String userInputString, TaskList tasks) throws DukeException {
        Task newlyAddedTask = TextToTaskTranslator.translateEventTask(userInputString);
        tasks.add(newlyAddedTask);

        String dukeFeedback = DukeTextFormatter.makeFormattedText(String.format(DukeUi.FEEDBACK_TASK_ADDED,
                                                                                newlyAddedTask.toString(),
                                                                                tasks.size()));
        return new DukeReply(false, true, dukeFeedback);
    }
}