package com.tixon.portlab.app;

import java.util.*;

public class Parser {
    public ArrayList<String> lexemes = new ArrayList<>();
    public HashMap<String, Double> vars = new HashMap<>();
    public HashMap<String, FunctionsDefinitions> functions = new HashMap<>();
    public ArrayList<Array> arrays = new ArrayList<>();
    public ArrayList<String> arrayHistory = new ArrayList<>();
    public ArrayList<String> arrayHistoryRaw = new ArrayList<>();
    public ArrayList<Array> arraysDefined = new ArrayList<>();
    public ArrayList<Error> errorList = new ArrayList<>();
    boolean variableExists = false;

    public Parser() {
        vars.put("pi", Math.PI);
        vars.put("e", Math.E);
    }

    public void initial() {
        vars.put("pi", Math.PI);
        vars.put("e", Math.E);
    }

    public boolean isOperator(Character ch) {
        boolean res = false;
        switch(ch) {
            //Modified 01-03-2015, added: '{', '}': arrays
            //Modified 03-03-2015 02:32, added: ';': arrays
            case '+':case '-':case '*':case '/':case'^':case'(':case')':case',':case'E':case'{':case'}':case';': res = true;
        }
        return res;
    }

    public void ErrorHandler(ArrayList<Error> E, int err) {
        //Modified 08-09-2014 21:44
        //See the values in 'values?-?/strings'
        Error error = new Error();
        if(err == 1) error.description = "var";
        if(err == 2) error.description = "bracket";
        if(err == 3) error.description = "const";
        E.add(error);
    }

    public void FindErrors(ArrayList<String> L) {
        Error error = new Error();
        for(int i = 0; i < L.size()-1; i++) {
            if(isIdentifier(L.get(i)) && !L.get(i+1).equals("(")) {
                ErrorHandler(errorList, error, L.get(i), 1);
            }
        }
        int j = L.size() - 1;
        if(isIdentifier(L.get(j))) {
            ErrorHandler(errorList, error, L.get(j), 1);
        }
    }

    public void ErrorHandler(ArrayList<Error> E, Error error, String s, int err) {
        //Modified 11-03-2015 00:07 (last modify 08-09-2014 21:44)
        if(err == 1) {
            error.description = "var";
            error.what.add(s);
        }
        if(err == 2) error.description = "bracket";
        if(err == 3) error.description = "const";
        E.add(error);
    }

    public void ErrorPrint(ArrayList<String> E) {
        if(!E.isEmpty()) {
            for (String i : E) {
                System.err.println(i);
            }
        }
    }

    public boolean isLP(Character ch) {
        return ch == '(';
    }

    public boolean isIdentifier(String s) {
        boolean res = false;
        int i = 0;
        if(Character.isDigit(s.charAt(i))) res = false;
        if(Character.isLetter(s.charAt(i))) {
            res = true;
            for(int j = 1; j < s.length(); j++) {
                res = Character.isLetterOrDigit(s.charAt(i));
            }
        }
        return res;
    }

    public boolean isDigit(String s) {
        int p = 0;
        int l = s.length() -1;
        boolean res = false;
        while(p <= l) {
            if(Character.isDigit(s.charAt(p))) {
                res = true;
                if(s.charAt(p) == '.') {
                    res = true;
                }
            }
            if(!Character.isDigit(s.charAt(p)) && s.charAt(p) != '.') {
                res = false;
                break;
            }
            p += 1;
        }
        return res;
    }

    public boolean ContainsIdentifier(ArrayList<String> L) {
        boolean res = false;
        for(int i = 0; i < L.size(); i++) {
            if(isIdentifier(L.get(i))) {
                res = true;
                break;
            }
        }
        return res;
    }

    public boolean ContainsAssignment(String s) {
        return s.contains(":=");
    }

    public void ParsingSet(ArrayList<String> F) {
        Function(F);
        Bracket(F);
        Power(F);
        MulDiv(F);
        PlusMinus(F);
    }

    public void setVars(String name, double value) {
        vars.put(name, value);
    }

    public void assignOperation(String s) {
        ArrayList<String> F = new ArrayList<>();
        int index;
        int LBracketIndex, RBracketIndex;
        String left, right;
        index = s.indexOf(":=");
        left = s.substring(0, index);
        right = s.substring(index+2);

        //Array
        //Modified 01-03-2015: arrays
        //Arrays
        if(right.contains("{") && right.contains("}")) {
            MakeListOfLexemes(right, F);
            ArrayList<Double> tempArrayValues = new ArrayList<>();
            if(!left.equals("pi") && !left.equals("e")) {
                DefineVariables(F, vars);
                Array(F, 15);
                //Delete variable with this name (left)
                if(vars.containsKey(left)) {
                    vars.remove(left);
                }
                //Delete array with this name (left) because 'arrays' is an ArrayList, not HashMap
                if(findItemPositionByName(arrays, left) != -1) {
                    arrays.remove(findItemPositionByName(arrays, left));
                }
                for(int i = 0; i < arrayHistoryRaw.size(); i++) {
                    tempArrayValues.add(Double.parseDouble(arrayHistoryRaw.get(i)));
                }
                arrayHistory.clear();
                arrayHistoryRaw.clear();
                Array tempArray = new Array();
                tempArray.setName(left);
                tempArray.setValues(tempArrayValues);
                arrays.add(tempArray);
            }
        } else {
            //Variable
            if(!left.contains("(") && !left.contains(")")) {
                MakeListOfLexemes(right, F);
                if(!left.equals("pi") && !left.equals("e")) {
                    //Handling of a right part
                    //MakeListOfLexemes(right, F);
                    DefineVariables(F, vars);
                    ParsingSet(F);
                    //Assignation of a variable
                    if(findItemPositionByName(arrays, left) != -1) {
                        arrays.remove(findItemPositionByName(arrays, left));
                    }
                    setVars(left, Double.parseDouble(F.get(0)));
                }
                else {
                    ErrorHandler(errorList, 3);
                }
            }
            //Function
            else if(left.contains("(") && left.contains(")")) {
                String name;
                FunctionsDefinitions tempFD = new FunctionsDefinitions();
                tempFD.arguments = new ArrayList<>();
                LBracketIndex = left.indexOf("(");
                RBracketIndex = left.indexOf(")");
                name = s.substring(0,LBracketIndex);
                String argumentsList = left.substring(LBracketIndex+1, RBracketIndex); //Select substring with arguments

                String[] argsMas = argumentsList.split(",");
                StringMasToList(argsMas, tempFD.arguments);

                ArrayList<String> tempL = new ArrayList<String>();
                MakeListOfLexemes(right, tempL);

                if(CheckBrackets(tempL)) {
                    tempFD.expression = right;
                    //FindErrors(tempL); //Сделать такую же процедуру, но с локальными переменными функции
                    if(errorList.isEmpty()) {
                        functions.put(name, tempFD);
                    }
                }
                else {
                    //Modified 11-03-2015 00:21
                    ErrorHandler(errorList, 2);
                    //FindErrors(tempL);
                }
            }
        }

        //ErrorPrint(errorList); //Modified 11-03-2015 00:20
        errorList.clear();
        lexemes.clear();
    }

    //Public since 08-03-2015
    public void StringMasToList(String[] mas, ArrayList<String> F) {
        for(int i = 0; i < mas.length; i++) {
            F.add(mas[i]);
        }
    }

    public int DelimiterCounter(ArrayList<String> F) {
        int count = 0;
        for(int i = 0; i < F.size(); i++) {
            if(F.get(i).equals(",")) {
                count += 1;
            }
        }
        return count;
    }

    public void MakeListOfLexemes(String s, ArrayList<String> L) {
        int p = 0;
        String f = "";
        int l = s.length()-1;
        while(p <= l) {
            if(isOperator(s.charAt(p))) {
                if(!f.isEmpty()) L.add(f);
                f = String.valueOf(s.charAt(p));
                if(f.equals("E")) {
                    if(p == 0) {
                        L.add("10"); L.add("^");
                    }
                    if(p != 0) {
                        L.add("*"); L.add("10"); L.add("^");
                    }
                }
                else {
                    L.add(f);
                }
                p += 1;
                f = "";
            }
            else {
                f += s.charAt(p);
                p += 1;
            }
        }
        if(!f.isEmpty()) L.add(f);
    }

    private String getVariable(HashMap<String, Double> v, String key) {
        double value;
        String res;
        if(v.containsKey(key)) {
            value = v.get(key);
            res = String.valueOf(value);
            variableExists = true;
        }
        else {
            variableExists = false;
            res = key;
        }
        return res;
    }

    private ArrayList<String> getArray(ArrayList<Array> arrays, String arrayName) {
        boolean contains = false;
        ArrayList<Double> values;
        ArrayList<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int position = findItemPositionByName(arrays, arrayName);
        if(position != -1) {
            values = arrays.get(position).getValues();
            res.add("{");
            sb.append("{");
            for(int i = 0; i < values.size()-1; i++) {
                res.add(String.valueOf(values.get(i)));
                res.add(";");
                sb.append(String.valueOf(values.get(i)));
                sb.append("; ");
            }
            res.add(String.valueOf(values.get(values.size()-1)));
            res.add("}");
            sb.append(String.valueOf(values.get(values.size()-1)));
            sb.append("}");
        }
        else {
            res.add(arrayName);
        }
        Array array = new Array();
        array.setName(arrayName);
        array.setValuesString(sb.toString());
        if(!arraysDefined.isEmpty()) {
            for(int i = 0; i < arraysDefined.size(); i++) {
                if(arraysDefined.get(i).getName().equals(arrayName)) {
                    //arraysDefined.add(array);
                    contains = true;
                    break;
                }
            }
            if(!contains) arraysDefined.add(array);
        } else {
            arraysDefined.add(array);
        }
        //if(!arraysDefined.contains(array)) arraysDefined.add(array);
        return res;
    }

    public void DefineVariables(ArrayList<String> L, HashMap<String, Double> v) {
        String var;
        for(int i = 0; i < L.size(); i++) {
            if(i != L.size()-1) {
                if(isIdentifier(L.get(i))) {
                    if(L.get(i+1).length() == 1) {
                        if(!isLP(L.get(i+1).charAt(0))) {
                            //getVariable and replace L.get(i)
                            var = getVariable(v, L.get(i));
                            L.set(i, var);
                        }
                    }
                }
            }
            if(i == L.size()-1) {
                if(isIdentifier(L.get(i))) {
                    //getVariable and replace L.get(i)
                    var = getVariable(v, L.get(i));
                    L.set(i, var);}
            }
        }
    }

    //defineArrays
    public void DefineArrays(ArrayList<String> L, ArrayList<Array> arrays) {
        ArrayList<String> array;
        for(int i = 0; i < L.size(); i++) {
            if(i != L.size()-1) {
                if(isIdentifier(L.get(i))) {
                    if(L.get(i+1).length() == 1) {
                        if(!isLP(L.get(i+1).charAt(0))) {
                            //getArray and replace L.get(i)
                            array = getArray(arrays, L.get(i));
                            for(int a = array.size()-1; a >= 0; a--) {
                                L.add(i+1, array.get(a));
                            }
                            L.remove(i);
                        }
                    }
                }
            }
            if(i == L.size()-1) {
                if(isIdentifier(L.get(i))) {
                    //getVariable and replace L.get(i)
                    array = getArray(arrays, L.get(i));
                    for(int a = array.size()-1; a >= 0; a--) {
                        L.add(i+1, array.get(a));
                    }
                    L.remove(i);
                }
            }
        }
    }

    public boolean CheckBrackets(ArrayList<String> L) {
        int PCounter = 0;
        for(int i = 0; i < L.size(); i++) {
            if(L.get(i).equals("(")) {
                PCounter += 1;
                //Pos = i;
                //position.add(Pos);
            }
            if(L.get(i).equals(")")) {
                //priority.add(PCounter);
                PCounter -= 1;
            }
        }
        if(PCounter != 0) ErrorHandler(errorList, 2);
        return PCounter == 0;
    }

    public void PlusMinus(ArrayList<String> L) {
        double res;
        int Pos;
        while(L.contains("-")) {
            Pos = L.indexOf("-");

            if(L.indexOf("-") == 0) {
                L.add(0, "0");
                Pos += 1;
            }

            res = Double.parseDouble(L.get(Pos - 1));
            res -= Double.parseDouble(L.get(Pos + 1));
            L.remove(Pos);
            L.remove(Pos);
            L.remove(Pos-1);
            L.add(Pos-1, String.valueOf(res));
            /*//Created 04-09-2014 23:16
            ArrayList<String> list_temp = new ArrayList<String>();
            MakeListOfLexemes(String.valueOf(res), list_temp);
            for(int i = 0; i < list_temp.size(); i++) {
                L.add(Pos-1+i, list_temp.get(i));
            }
            //End of: Created 04-09-2014 23:16*/
            //MODIFIED!!!!!

        }

        while(L.contains("+")) {
            Pos = L.indexOf("+");
            res = Double.parseDouble(L.get(Pos - 1));
            res += Double.parseDouble(L.get(Pos + 1));
            L.remove(Pos);
            L.remove(Pos);
            L.remove(Pos-1);
            L.add(Pos-1, String.valueOf(res));
        }
    }

    public void MulDiv(ArrayList<String> L) {
        double res;
        int Pos;
        while(L.contains("*")) {
            Pos = L.indexOf("*");
            double left = Double.parseDouble(L.get(Pos - 1));
            double right = Double.parseDouble(L.get(Pos + 1));
            String temp = String.valueOf(left * right);
            if(temp.equals("-0.0")) {
                StringBuilder sb = new StringBuilder();
                sb.append(temp);
                sb.deleteCharAt(0);
                temp = sb.toString();
            }
            res = Double.parseDouble(temp);
            L.remove(Pos);
            L.remove(Pos);
            L.remove(Pos-1);
            L.add(Pos-1, String.valueOf(res));
        }
        while(L.contains("/")) {
            Pos = L.indexOf("/");
            res = Double.parseDouble(L.get(Pos - 1));
            res /= Double.parseDouble(L.get(Pos + 1));
            L.remove(Pos);
            L.remove(Pos);
            L.remove(Pos-1);
            L.add(Pos-1, String.valueOf(res));
        }
    }

    public void Power(ArrayList<String> L) {
        int Pos;
        double res;
        double n1, n2;
        while(L.contains("^")) {
            Pos = L.lastIndexOf("^");
            n1 = Double.parseDouble(L.get(Pos - 1));
            if(L.get(Pos + 1).equals("-")) {
                n2 = Double.parseDouble(L.get(Pos + 1) + L.get(Pos + 2));
            } else {
                n2 = Double.parseDouble(L.get(Pos + 1));
            }
            res = Math.pow(n1, n2);
            L.remove(Pos);
            L.remove(Pos);
            L.remove(Pos-1);
            L.add(Pos-1, String.valueOf(res));
        }
    }

    public void Bracket(ArrayList<String> L) {
        while(L.contains("(") || L.contains(")")) { //Сделаем для проверки одну итерацию
            int pos = 0;
            ArrayList<String> F = new ArrayList<>();
            for(int i = 0; i < L.size(); i++) {
                if(L.get(i).equals(")")) { //Находим закрывающуюся скобку, и ищем открывающую сразу до неё
                    int j = i - 1;
                    while(!L.get(j).equals("(")) {
                        F.add(0, L.get(j));
                        L.remove(j);
                        j -= 1;
                    }
                    pos = j;
                    L.remove(j); L.remove(j); //Удалить скобки
                    break;
                }
            }
            Function(F);
            Power(F);
            MulDiv(F);
            PlusMinus(F);
            L.add(pos, F.get(0));
        }
    }

    public void Function(ArrayList<String> L) {
        int BracketCounter = 0;
        int LBracketIndex;
        double res;
        String name;
        ArrayList<String> betweenBrackets = new ArrayList<>();
        ArrayList<String> tempList = new ArrayList<>();
        ArrayList<String> forEquation = new ArrayList<>();
        for(int i = 0; i < L.size()-1; i++) {
            if(isIdentifier(L.get(i))) { //Если лексема - идентификатор
                if(L.get(i+1).equals("(")) { //Если следующая лексема - открывающая скобка
                    //Значит, мы попали в функцию. Можно заносить её значение в ArrayList аргументов функции
                    LBracketIndex = i+1;
                    int index;
                    index = LBracketIndex + 1;
                    BracketCounter += 1;

                    //Вставляем элемент из lexeme в betweenBrackets, а потом удаляем из lexeme.
                    //Индекс при этом не меняется
                    while(true) {
                        if(L.get(index).equals("(")) BracketCounter += 1;
                        if(L.get(index).equals(")")) BracketCounter -= 1;
                        if(BracketCounter == 0) break;
                        betweenBrackets.add(L.get(index));
                        L.remove(index);
                    } //Мы только что перевели аргументы функции в ArrayList<String> betweenBrackets

                    //Удаляем оставшиеся скобки - они остались от функции
                    L.remove(index);
                    L.remove(index-1);

                    //Записываем имя функции в переменную, удаляем из списка лексем и переходим к обработке
                    name = L.get(index-2);
                    L.remove(index-2);

                    BracketCounter = 0;
                    ArrayList<String> argumentsToParse = new ArrayList<>();
                    String str = "";

                    for(int k = 0; k < betweenBrackets.size(); k++) {
                        if(betweenBrackets.get(k).equals("(")) BracketCounter += 1;
                        if(betweenBrackets.get(k).equals(")")) BracketCounter -= 1;
                        str += betweenBrackets.get(k);
                        if(BracketCounter == 0 && betweenBrackets.get(k).equals(",")) {
                            str = str.substring(0, str.length()-1);
                            argumentsToParse.add(str);
                            str = "";
                            //ToDo: this is in the Array function
                        }
                        if(k == betweenBrackets.size()-1) {
                            argumentsToParse.add(str);
                            str = "";
                        }
                    } //Здесь мы получаем ArrayList<String> argumentsToParse, в котором есть строки, готовые к парсингу

                    betweenBrackets.clear();

                    //Парсируем весь argumentsToParse, если конечно нужно

                    for(int k = 0; k < argumentsToParse.size(); k++) {
                        ArrayList<String> tempParsingList = new ArrayList<>();
                        MakeListOfLexemes(argumentsToParse.get(k), tempParsingList);
                        ParsingSet(tempParsingList);

                        argumentsToParse.remove(k);
                        argumentsToParse.add(k, tempParsingList.get(0));
                    }

                    if(argumentsToParse.size() == 1) { //Если у нас только один элемент, то имеем особый случай
                        betweenBrackets.add(argumentsToParse.get(0));
                    }
                    if(argumentsToParse.size() > 1) { //А если много элементов, то добавляем сначала элемент, потом запятую
                        for(int k = 0; k < argumentsToParse.size() - 1; k++) {
                            betweenBrackets.add(argumentsToParse.get(k));
                            betweenBrackets.add(",");
                        }
                        betweenBrackets.add(argumentsToParse.get(argumentsToParse.size()-1)); //Добавляем последний элемент
                    }


                    int DCounter = DelimiterCounter(betweenBrackets);

                    for(int m = 0; m < DCounter+1; m++) {
                        //if(betweenBrackets.contains(",")) {
                        //while(betweenBrackets.contains(",")) {
                        //Расчитываем выражение
                        int indexDelimiter = betweenBrackets.indexOf(",");
                        while(true) {
                            if(betweenBrackets.isEmpty()) break;
                            if(betweenBrackets.get(0).equals(",")) {
                                betweenBrackets.remove(0);
                                break;
                            }
                            tempList.add(betweenBrackets.get(0));
                            betweenBrackets.remove(0);
                        }
                        //betweenBrackets.remove(0);
                        ParsingSet(tempList);

                        forEquation.add(tempList.get(0));
                        tempList.clear();
                    }
                    //Добавляем в lexemes просчитанный результат функции
                    res = ProcessFunction(forEquation, name);
                    forEquation.clear();
                    L.add(index-2, String.valueOf(res));
                }
            }
        }
    }

    public double ProcessFunction(ArrayList<String> expression, String name) {
        //int numberOfArguments;
        double result = 0;
        if(name.equals("cos")) {
            result = Math.cos(Math.toRadians(Double.parseDouble(expression.get(0))));
        }
        else if(name.equals("sin")) {
            result = Math.sin(Math.toRadians(Double.parseDouble(expression.get(0))));
        }
        else if(name.equals("tg")) {
            result = Math.tan(Math.toRadians(Double.parseDouble(expression.get(0))));
        }
        else if(name.equals("ctg")) {
            result = Math.tan(Math.toRadians(90 - Double.parseDouble(expression.get(0))));
        }
        else if(name.equals("rad")) {
            result = Double.parseDouble(expression.get(0)) * Math.PI / 180;
        }
        else if(name.equals("deg")) {
            result = Double.parseDouble(expression.get(0)) * 180 / Math.PI;
        }
        else if(name.equals("ln")) {
            result = Math.log(Double.parseDouble(expression.get(0)));
        }
        else if(name.equals("lg")) {
            result = Math.log10(Double.parseDouble(expression.get(0)));
        }
        else if(name.equals("sqrt")) {
            result = Math.sqrt(Double.parseDouble(expression.get(0)));
        }
        else if(name.equals("abs")) {
            result = Math.abs(Double.parseDouble(expression.get(0)));
        }
        else if(name.equals("log")) {
            if(expression.size() == 1) {
                result = Math.log10(Double.parseDouble(expression.get(0)));
            } else {
                double state, expr;
                state = Double.parseDouble(expression.get(0));
                expr = Double.parseDouble(expression.get(1));
                state = Math.log(state);
                expr = Math.log(expr);
                result = expr/state;
            }
        }
        else if(name.equals("sum")) {
            for(int i = 0; i < expression.size(); i++) {
                result += Double.parseDouble(expression.get(i));
            }
        }
        /*else if(name.equals("sum")) {
            double start, end, step, temp_var_value;
            String var, expr, temp_var_name;
            start = Double.parseDouble(expression.get(0));
            end = Double.parseDouble(expression.get(1));
            step = Double.parseDouble(expression.get(2));
            var = expression.get(3);
            expr = expression.get(4);
            if(vars.containsKey(var)) {
                temp_var_name = var;
                temp_var_value = vars.get(var);

                for(double k = start; k <= end; k+=step) {
                    vars.put(var, k);

                }
            }
        }*/
        //Теперь пошли собственные функции, проверяем HashMap на наличие ключа
        else{
            if(functions.containsKey(name)) {
                FunctionsDefinitions tempFD;
                ArrayList<String> L = new ArrayList<>();
                tempFD = functions.get(name); //Здесь получаем аргументы функции и её выражение
                //Теперь должны подставить в аргументы наши значения
                //Создаём HashMap локальных переменных localVariables
                HashMap<String, Double> localVariables = new HashMap<>();
                if(expression.size() == tempFD.arguments.size()) {
                    for(int i = 0; i < expression.size(); i++) {
                        localVariables.put(tempFD.arguments.get(i), Double.parseDouble(expression.get(i)));
                    }
                    MakeListOfLexemes(tempFD.expression, L);
                    DefineVariables(L, localVariables);
                    DefineVariables(L, vars); //Это на случай, если в строке есть переменные-константы, не входящие в аргументы функции
                    ParsingSet(L);
                    result = Double.parseDouble(L.get(0));
                }
            }
        }
        return result;
    }

    //Todo: I was here 01-03-2015
    public void Array(ArrayList<String> L, int round) {
        ArrayList<ArrayList<String>> currentArrays = new ArrayList<>();
        ArrayList<String> arrayItems = new ArrayList<>();
        ArrayList<String> betweenBrackets = new ArrayList<>();
        ArrayList<String> tempLexemes = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        int arrayCounter = -1;
        int arraySize;
        boolean similarSizes = true;
        String s = "";
        String tempHistoryString = "";

        if(L.contains("{") || L.contains("}")) {//It is an array
            for(int i = 0; i < L.size(); i++) {
                if(L.get(i).equals("{")) {//If it's a massive
                    indexes.add(i+1);//index = i+1;
                    arrayCounter += 1;
                    while(!L.get(indexes.get(arrayCounter)).equals("}")) {
                        betweenBrackets.add(L.get(indexes.get(arrayCounter)));
                        L.remove((int)indexes.get(arrayCounter));
                    }
                    L.remove((int)indexes.get(arrayCounter));
                    L.remove(indexes.get(arrayCounter)-1);
                    currentArrays.add(betweenBrackets);
                    betweenBrackets = new ArrayList<>();
                }
            }

            for (int a = 0; a < currentArrays.size(); a++) {//a = array (one of currentArrays)
                ArrayList<String> currentArray;// = new ArrayList<String>();
                currentArray = currentArrays.get(a);
                for(int i = 0; i < currentArray.size(); i++) {
                    s += currentArray.get(i);
                    if(currentArray.get(i).equals(";")) {
                        s = s.substring(0, s.length()-1);
                        arrayItems.add(s);
                        s = "";
                    }
                    if(i == currentArray.size()-1) {
                        arrayItems.add(s);
                        s = "";
                    }
                }
                currentArrays.set(a, arrayItems);
                arrayItems = new ArrayList<>();
            }

            //Todo: Check if arrays' sizes are not similar

            arraySize = currentArrays.get(0).size();
            for(int a = 1; a < currentArrays.size(); a++) {
                if(currentArrays.get(a).size() != arraySize) similarSizes = false;
            }

            if(similarSizes) {//Continue working
                int size = currentArrays.get(0).size();
                //Parse all elements in all arrays
                for(int i = 0; i < size; i++) {
                    for(int a = 0; a < currentArrays.size(); a++) {
                        ArrayList<String> lexemesOfArrayItem = new ArrayList<>();
                        MakeListOfLexemes(currentArrays.get(a).get(i), lexemesOfArrayItem);
                        ParsingSet(lexemesOfArrayItem);
                        currentArrays.get(a).set(i, lexemesOfArrayItem.get(0));
                    }
                }

                if(!lexemes.isEmpty()) for (int k = 0; k < lexemes.size(); k++)
                    tempLexemes.add(lexemes.get(k));

                for (int a = 0; a < currentArrays.size(); a++) {
                    tempLexemes.add(indexes.get(a)-1+a, currentArrays.get(a).get(0));
                }
                //Todo: tempHistoryString
                tempHistoryString = makeStringFromList(tempLexemes);
                ParsingSet(tempLexemes);
                String currentResult = tempLexemes.get(0);
                try {
                    currentResult = Round(tempLexemes.get(0), round);
                } catch (Exception e) {
                    //Some exceptions in Round()
                }
                arrayHistory.add("index: 0; " + tempHistoryString + " = " + currentResult);
                arrayHistoryRaw.add(currentResult);
                tempLexemes.clear();

                for (int i = 1; i < size; i++) {
                    if(!lexemes.isEmpty()) for (int k = 0; k < lexemes.size(); k++)
                        tempLexemes.add(lexemes.get(k));
                    for (int a = 0; a < currentArrays.size(); a++) {
                        tempLexemes.add(indexes.get(a)-1+a, currentArrays.get(a).get(i));
                    }
                    tempHistoryString = makeStringFromList(tempLexemes);
                    ParsingSet(tempLexemes);
                    currentResult = tempLexemes.get(0);
                    try {
                        currentResult = Round(tempLexemes.get(0), round);
                    } catch (Exception e) {
                        //Some exceptions in Round()
                    }
                    arrayHistory.add("index: " + i + "; " + tempHistoryString + " = " + currentResult);
                    arrayHistoryRaw.add(currentResult);
                    tempLexemes.clear();
                }
            } else {
                //An error occurred - not similar sizes!
            }
        } else {//It is not an array
            ParsingSet(L); //Compute as an expression without arrays
        }
    }

    //Todo: static from 08-03-2015
    public static String makeStringFromList(ArrayList<String> L) {
        String result = "";
        for(int i = 0; i < L.size(); i++) {
            result += L.get(i);
        }
        return result;
    }

    private int FindMaxIndex(ArrayList<Integer> L) {
        int maxIndex, max;
        max = L.get(0);
        maxIndex = 0;
        for(int i = 1; i < L.size(); i++) {
            if(L.get(i) > max) {
                max = L.get(i);
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public String deleteSpaces(String s) {
        StringBuilder SB = new StringBuilder();
        SB.append(s);
        while(SB.indexOf(" ") != -1) {
            SB.deleteCharAt(SB.indexOf(" "));
        }
        return SB.toString();
    }

    boolean checkBrackets(String s) {
        StringBuilder sb = new StringBuilder();
        int balance = 0;
        sb.append(s);
        for(int i = 0; i < sb.length(); i++) {
            if(String.valueOf(sb.charAt(i)).equals("(")) balance += 1;
            if(String.valueOf(sb.charAt(i)).equals(")")) balance -= 1;
        }
        return balance == 0;
    }

    int findItemPositionByName(ArrayList<Array> arrayList, String arrayName) {
        int position = -1;
        for(int i = 0; i < arrayList.size(); i++) {
            if(arrayList.get(i).getName().equals(arrayName)) {
                position = i; break;
            }
        }
        return position;
    }

    String Round(String s, int number) {
        ArrayList<Integer> fList = new ArrayList<>();
        StringBuilder sF = new StringBuilder().append(s.substring(s.indexOf(".") + 1));
        StringBuilder sF_after = new StringBuilder();
        StringBuilder E = new StringBuilder();
        StringBuilder result = new StringBuilder();
        StringBuilder sD_string = new StringBuilder().append(s.substring(0, s.indexOf(".")));
        long sD;
        boolean flag = false;
        boolean containsE = false;
        boolean first = true;
        boolean negative = false;

        //Проверка отрицательного числа
        if(String.valueOf(sD_string.charAt(0)).equals("-")) {
            negative = true;
            sD = Long.parseLong(sD_string.substring(1));
        } else {
            sD = Long.parseLong(sD_string.toString());
        }

        //Если число содержит E (напр. 2.91626543E3)
        if(s.contains("E")) {
            containsE = true;
            E.append(s.substring(s.indexOf("E")));
            s = s.substring(0, s.indexOf("E"));
        }

        if(sF.length() > number) {
            for(int i = 0; i < number; i++) fList.add(Integer.parseInt(String.valueOf(sF.charAt(i))));

            int control = Integer.parseInt(String.valueOf(sF.charAt(number)));
            int memory = Integer.parseInt(String.valueOf(sF.charAt(number-1)));
            if(control >= 5) {
                for(int i = fList.size()-1; i >= 0; i--) {
                    if(fList.get(i) < 9) { //If digit < 9
                        if(memory >= 5) {
                            fList.set(i, fList.get(i) + 1);
                        }
                        break;
                    }

                    if(fList.get(i) == 9) { //If digit = 9
                        if(memory >= 5) {
                            memory = fList.get(i);
                            fList.set(i, (fList.get(i) + 1) % 10);
                            if(i == 0) {
                                flag = true;
                            }
                        }
                    }
                }
            }
            for(int i = fList.size() - 1; i >= 0; i--) {
                int digit = fList.get(i);
                if((i == 0) && (digit == 0)) {
                    sF_after.append(digit);
                    break;
                }
                if(digit > 0) { //Was: (digit > 0) && (first)
                    sF_after.append(digit);
                    first = false;
                }
                if((digit == 0) && (!first)) {
                    sF_after.append(digit);
                }
            }
            sF_after.reverse();

            if(flag) sD += 1;
            if(negative) result.append("-");
            result.append(sD)
                    .append(".")
                    .append(sF_after);
            //was: if(containsE) result.append(E);
        }
        else {
            result.append(s);
        }
        if(containsE) result.append(E);
        return result.toString();
    }
}
