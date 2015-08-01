package com.tixon.portlab.app;

import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;

public class Matrix extends Parser {
    //public double[][] Matrix;
    //private int n;

    public double det(ArrayList<ArrayList<Double>> m, StringBuilder log) {
        double D = 0;
        int n = m.size();
        ArrayList<ArrayList<Double>> l_minor;
        switch(n) {
            case 1:
                D = m.get(0).get(0);
                log.append(D);
                break;
            case 2:
                D = m.get(0).get(0) * m.get(1).get(1) - m.get(0).get(1) * m.get(1).get(0);
                log.append(m.get(0).get(0)).append(" * ");
                if(m.get(1).get(1) < 0) log.append("(").append(m.get(1).get(1)).append(")");
                else log.append(m.get(1).get(1));
                if(m.get(0).get(1) < 0) log.append(" + ").append(m.get(0).get(1) * (-1));
                else log.append(" - ").append(m.get(0).get(1));
                log.append(" * ");
                if(m.get(1).get(0) < 0) log.append("(").append(m.get(1).get(0)).append(")");
                else log.append(m.get(1).get(0));
                //generalized
                log.append(" = ");
                log.append(m.get(0).get(0) * m.get(1).get(1));
                if(m.get(0).get(1) * m.get(1).get(0) < 0) log.append(" + ").append(m.get(0).get(1) * m.get(1).get(0) *  (-1));
                else log.append(" - ").append(m.get(0).get(1) * m.get(1).get(0));
                //result
                log.append(" = ").append(D);
                break;
            case 3:
                double A, B;
                for(int j = 0; j<n; j++) {
                    A = 1; B = 1;
                    for(int i = 0; i<n; i++) {
                        A *= m.get(i).get((j+i) % n);
                        B *= m.get(n-i-1).get((j+i) % n);
                    }
                    D += A - B;
                }
                break;
            default:
                int sign = 1;
                double[][] m_minor;
                for(int k = 0; k<n; k++) {
                    m_minor = new double[n-1][n-1];
                    //
                    for(int i=1; i<n; i++) {
                        for(int j=0; j<k; j++) m_minor[i-1][j] = m.get(i).get(j);
                        for(int j=k+1; j<n; j++) m_minor[i-1][j-1] = m.get(i).get(j);
                    }
                    l_minor = TransformMasToList(m_minor, n-1, n-1);//
                    D += sign * m.get(0).get(k) * det(l_minor, log);
                    sign *= (-1);
                }
                break;
        }
        return D;
    }

    public ArrayList<ArrayList<Double>> read(EditText editText) {
        ArrayList<ArrayList<Double>> matrix;
        ArrayList<String> tempList;
        String text = editText.getText().toString();
        tempList = createListOfStrings(text);
        matrix = CreateMatrix(tempList);
        return matrix;
    }

    /*public void setSize(ArrayList<ArrayList<Double>> list) {
        ArrayList<Double> temp;
        temp = list.get(0);
        this.Matrix = new double[list.size()][temp.size()];
        this.n = list.size();
    }*/

    public void ParseMatrixItems(ArrayList<ArrayList<String>> matrix) {
        for (int i = 0; i < matrix.size(); i++) {
            ArrayList<String> tempList;
            tempList = matrix.get(i);
            for (int j = 0; j < tempList.size(); j++) {
                String s = tempList.get(j);
                ArrayList<String> lexemes = new ArrayList<>();
                s = deleteSpaces(s);
                MakeListOfLexemes(s, lexemes);
                ParsingSet(lexemes);
                tempList.set(j, lexemes.get(0));
            }
            matrix.set(i, tempList);
        }
    }

    public ArrayList<ArrayList<Double>> SetMatrixDouble(ArrayList<ArrayList<String>> list) {
        ArrayList<String> tempList;
        ArrayList<Double> tempRow;
        ArrayList<ArrayList<Double>> result = new ArrayList<>();
        for (ArrayList<String> aList : list) {
            tempList = aList;
            tempRow = new ArrayList<Double>();
            for (String aTempList : tempList) {
                tempRow.add(Double.parseDouble(aTempList));
            }
            result.add(tempRow);
        }
        return result;
    }

    public boolean IsMatrixSquare(ArrayList<ArrayList<Double>> matrix) {
        boolean f;
        ArrayList<Double> tempRow;
        tempRow = matrix.get(0);
        f = matrix.size() == tempRow.size();
        return f;
    }

    public boolean AreConsistent(ArrayList<ArrayList<Double>> matrix1, ArrayList<ArrayList<Double>> matrix2) {
        boolean f;
        ArrayList<Double> tempRow;
        int size_1, size_2;
        //size_1: number of columns (matrix1)
        //size_2: number of rows (matrix2)
        tempRow = matrix1.get(0);
        size_1 = tempRow.size();
        size_2 = matrix2.size();
        f = size_1 == size_2;
        return f;
    }

    public ArrayList<ArrayList<Double>> Multiply(ArrayList<ArrayList<Double>> matrix1, ArrayList<ArrayList<Double>> matrix2) {
        ArrayList<ArrayList<Double>> result;
        int m, q, n;
        n = matrix1.get(0).size();
        m = matrix1.size();
        q = matrix2.get(0).size();
        double[][] C = new double[m][q];
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < q; j++) {
                for(int r = 0; r < n; r++) {
                    C[i][j] += matrix1.get(i).get(r) * matrix2.get(r).get(j);
                }
            }
        }
        result = TransformMasToList(C, m, q);
        return result;
    }

    public ArrayList<ArrayList<Double>> Division (ArrayList<ArrayList<Double>> matrix1, ArrayList<ArrayList<Double>> matrix2) {
        ArrayList<ArrayList<Double>> matrix2_reverse = Reverse(matrix2);
        return Multiply(matrix1, matrix2_reverse);
    }

    public ArrayList<ArrayList<Double>> Addition(ArrayList<ArrayList<Double>> matrix1, ArrayList<ArrayList<Double>> matrix2) {
        int sizeRows = matrix1.size();
        int sizeColumns = matrix1.get(0).size();
        double[][] result = new double[sizeRows][sizeColumns];
        for(int i = 0; i < sizeRows; i++) {
            for(int j = 0; j < sizeColumns; j++) {
                result[i][j] = matrix1.get(i).get(j) + matrix2.get(i).get(j);
            }
        }
        return TransformMasToList(result, sizeRows, sizeColumns);
    }

    public ArrayList<ArrayList<Double>> Subtraction(ArrayList<ArrayList<Double>> matrix1, ArrayList<ArrayList<Double>> matrix2) {
        int sizeRows = matrix1.size();
        int sizeColumns = matrix1.get(0).size();
        double[][] result = new double[sizeRows][sizeColumns];
        for(int i = 0; i < sizeRows; i++) {
            for (int j = 0; j < sizeColumns; j++) {
                result[i][j] = matrix1.get(i).get(j) - matrix2.get(i).get(j);
            }
        }
        return TransformMasToList(result, sizeRows, sizeColumns);
    }

    public boolean SameSize(ArrayList<ArrayList<Double>> matrix1, ArrayList<ArrayList<Double>> matrix2) {
        boolean f;
        int size1_1, size1_2, size2_1, size2_2;
        size1_1 = matrix1.size(); size2_1 = matrix2.size();
        size1_2 = matrix1.get(0).size(); size2_2 = matrix2.get(0).size();
        f = ((size1_1 == size2_1) & (size1_2 == size2_2));
        return f;
    }

    public boolean ZeroDeterminant(ArrayList<ArrayList<Double>> matrix) {
        boolean f; double d;
        d = det(matrix, new StringBuilder());
        f = d == 0;
        return f;
    }

    public ArrayList<ArrayList<Double>> Transpose(ArrayList<ArrayList<Double>> matrix) {
        ArrayList<ArrayList<Double>> transposed;
        int sizeRows, sizeColumns;
        sizeRows = matrix.size();
        sizeColumns = matrix.get(0).size();
        double[][] m = new double[sizeColumns][sizeRows];
        for(int i = 0; i < sizeRows; i++) {
            for(int j = 0; j < sizeColumns; j++) {
                m[j][i] = matrix.get(i).get(j);
            }
        }
        transposed = TransformMasToList(m, sizeColumns, sizeRows);
        return transposed;
    }

    public ArrayList<ArrayList<Double>> CreateMinorMatrix(ArrayList<ArrayList<Double>> matrix) {
        int sizeRows = matrix.size();
        int sizeColumns = matrix.get(0).size();
        double[][] m = new double[sizeRows][sizeColumns];
        for(int i = 0; i < sizeRows; i++) {
            for(int j = 0; j < sizeColumns; j++) {
                m[i][j] = det(SelectMinor(matrix, i, j), new StringBuilder());
            }
        }
        return TransformMasToList(m, sizeRows, sizeColumns);
    }
    //Ru: Алгебраические дополнения
    public ArrayList<ArrayList<Double>> Cofactor(ArrayList<ArrayList<Double>> matrix) {
        int sizeRows = matrix.size();
        int sizeColumns = matrix.get(0).size();
        double[][] m = new double[sizeRows][sizeColumns];
        for(int i = 0; i < sizeRows; i++) {
            for(int j = 0; j < sizeColumns; j++) {
                m[i][j] = (matrix.get(i).get(j)) * (Math.pow(-1, i+j));
                if(String.valueOf(m[i][j]).equals("-0.0")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.valueOf(m[i][j]));
                    sb.deleteCharAt(0);
                    m[i][j] = Double.parseDouble(sb.toString());
                }
            }
        }
        return TransformMasToList(m, sizeRows, sizeColumns);
    }

    //Ru: выбрать минор
    ArrayList<ArrayList<Double>> SelectMinor(ArrayList<ArrayList<Double>> matrix, int row, int column) {
        int sizeRows = matrix.size();
        int sizeColumns = matrix.get(0).size();
        double[][] m = new double[sizeRows-1][sizeColumns-1];
        for(int i = 0; i < sizeRows; i++) {
            for(int j = 0; j < sizeColumns; j++) {
                if(i < row) {
                    if(j < column) {
                        m[i][j] = matrix.get(i).get(j);
                    }
                    if(j > column) {
                        m[i][j-1] = matrix.get(i).get(j);
                    }
                }
                if(i > row) {
                    if(j < column) {
                        m[i-1][j] = matrix.get(i).get(j);
                    }
                    if(j > column) {
                        m[i-1][j-1] = matrix.get(i).get(j);
                    }
                }
            }
        }
        return TransformMasToList(m, sizeRows-1, sizeColumns-1);
    }

    //Ru: умножение матрицы на число
    public ArrayList<ArrayList<Double>> MultiplyNumberToMatrix(ArrayList<ArrayList<Double>> matrix, double number) {
        int sizeRows = matrix.size();
        int sizeColumns = matrix.get(0).size();
        double[][] m = new double[sizeRows][sizeColumns];
        for(int i = 0; i < sizeRows; i++) {
            for(int j = 0; j < sizeColumns; j++) {
                m[i][j] = number * matrix.get(i).get(j);
            }
        }
        return TransformMasToList(m, sizeRows, sizeColumns);
    }

    ArrayList<ArrayList<Double>> Reverse(ArrayList<ArrayList<Double>> matrix) {
        //int sizeRows = matrix.size();
        //int sizeColumns = matrix.get(0).size();
        double d;
        //double[][] m = new double[sizeRows][sizeColumns];
        d = det(matrix, new StringBuilder());
        return  MultiplyNumberToMatrix(Transpose(Cofactor(CreateMinorMatrix(matrix))), 1 / d);
    }

    //Ru: след матрицы
    public double Trace(ArrayList<ArrayList<Double>> matrix) {
        double t = 0;
        int sizeRows = matrix.size();
        int sizeColumns = matrix.get(0).size();
        int k = 0;
        if(sizeRows > sizeColumns) k = sizeColumns;
        if(sizeRows < sizeColumns) k = sizeRows;
        if(sizeRows == sizeColumns) k = sizeRows;
        for(int i = 0; i < k; i++) {
            for(int j = 0; j < k; j++) {
                if(i == j) t += matrix.get(i).get(j);
            }
        }
        return t;
    }

    //Ru: возведение матрицы в степень
    ArrayList<ArrayList<Double>> Exponentiation(ArrayList<ArrayList<Double>> matrix, int exp) {
        ArrayList<ArrayList<Double>> exp_matrix = matrix;
        exp -= 1;
        for(int i = 0; i < exp; i++) {
            exp_matrix = Multiply(matrix, exp_matrix);
        }
        return exp_matrix;
    }

    //Not used
    /*public double[][] TransformListToMas(ArrayList<ArrayList<Double>> list) {
        int size = list.size();
        double[][] mas = new double[size][size];
        for(int i=0; i<size; i++) {
            ArrayList<Double> temp = new ArrayList<Double>();
            temp = list.get(i);
            for(int j=0; j<size; j++) {
                mas[i][j] = temp.get(j);
            }
        }
        setSize(list);
        return mas;
    }*/

    public ArrayList<ArrayList<Double>> TransformMasToList(double[][] mas, int size_rows, int size_columns) {
        ArrayList<ArrayList<Double>> list = new ArrayList<ArrayList<Double>>();
        for(int i=0; i<size_rows; i++) {
            ArrayList<Double> temp_list = new ArrayList<Double>();
            for(int j=0; j<size_columns; j++) {
                temp_list.add(mas[i][j]);
            }
            list.add(temp_list);
        }
        return list;
    }

    //From MainActivity of listViewMatrix

    public ArrayList<String> createListOfStrings(String text) {
        ArrayList<String> list = new ArrayList<String>();
        String[] mas = text.split("\n");
        Collections.addAll(list, mas);
        return list;
    }

    ArrayList<ArrayList<Double>> CreateMatrix(ArrayList<String> list) {
        ArrayList<String> temp;
        ArrayList<ArrayList<String>> matrix_string = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<Double>> matrix;
        for (String aList : list) {
            temp = new ArrayList<String>();
            String[] mas = aList.split(";");
            Collections.addAll(temp, mas);
            matrix_string.add(temp);
        }
        ParseMatrixItems(matrix_string);
        matrix = SetMatrixDouble(matrix_string);
        return matrix;
    }

    //Prepare Matrix to output in any text field
    public String MatrixToString(ArrayList<ArrayList<Double>> matrix, int round) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Double> tempRow;
        ArrayList<ArrayList<String>> m_string = DoubleToString(matrix, round);
        int maxSpaceNumber = GetMaxLength(m_string); // // //
        for (ArrayList<Double> aMatrix : matrix) {
            tempRow = aMatrix;
            for (Double aTempRow : tempRow) {
                sb.append(Space(maxSpaceNumber - Round(String.valueOf(aTempRow), round).length()));
                sb.append(Round(String.valueOf(aTempRow), round));
                sb.append("; ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    ArrayList<ArrayList<String>> DoubleToString(ArrayList<ArrayList<Double>> matrix, int round) {
        ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
        ArrayList<Double> temp_row_double;
        ArrayList<String> temp_row = new ArrayList<String>();
        for (ArrayList<Double> aMatrix : matrix) {
            temp_row_double = aMatrix;
            for (int j = 0; j < aMatrix.size(); j++) {
                temp_row.add(Round(String.valueOf(temp_row_double.get(j)), round));
            }
            temp.add(temp_row);
        }
        return temp;
    }

    int GetMaxLength(ArrayList<ArrayList<String>> matrix) {
        int max = 0;
        for (ArrayList<String> aMatrix : matrix) {
            for (String anAMatrix : aMatrix) {
                if (anAMatrix.length() > max) max = anAMatrix.length();
            }
        }
        return max;
    }
    
    String Space(int number) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < number; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}