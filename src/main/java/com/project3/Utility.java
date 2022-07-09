package com.project3;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Utility {
    private final Statement statement;
    public Utility (Statement statement) {
        this.statement = statement;
    }

    public void doPostHelper(HttpServletRequest request, String userType) {
        // Get the query from the text box
        String textBox = request.getParameter("textBox");
        String query = textBox.toLowerCase();
        HttpSession session = request.getSession();
        String result;
        String execute;
        String text;

        // Find out which button was clicked by the user
        String buttonClicked = request.getParameter("button_clicked");

        switch (buttonClicked) {
            // User clicked the execute button
            case "Execute":
                executeClicked(query, session, textBox, userType);
                break;
            // User clicked the clear button
            case "Clear":
                // Clear the textBox
                text = "";
                session.setAttribute("textBox", text);
                execute = "<div class = \"executionContainer\"><p class = \"executionText\">Text box cleared...</p></div>";
                session.setAttribute("execute", execute);
                break;
            // User clicked the reset table button
            case "Reset Table":
                // Clear and reset the table
                result = null;
                session.setAttribute("result", result);
                // Show that the table was cleared
                execute = "<div class = \"executionContainer\"><p class = \"executionText\">Table cleared...</p></div>";
                session.setAttribute("execute", execute);
                break;
        }

        System.out.println(buttonClicked);
    }

    public void executeClicked(String query, HttpSession session, String textBox, String userType) {
        String result;
        String execute = null;
        String text;

        // Check if the query is SELECT
        if (query.contains("select")) {
            try {
                // Run the query
                result = selectQuery(query);
                // Update the table
                session.setAttribute("result", result);
                // Show to the user that the command was successful
                execute = "<div class = \"executionContainerGood\"><p class = \"executionText\">Command Successful</p></div>";
            } catch (SQLException e) {
                // Show any errors to the user
                execute = "<div class = \"executionContainerBad\"><p class = \"executionText\">" + e.getMessage() + "</p></div>";
                e.printStackTrace();
            }
            session.setAttribute("execute", execute);
        }
        // If the execute button was clicked and there is no query
        else if (query.equals("")) {
            // Tell the user that there are no query entered
            execute = "<div class = \"executionContainerGood\"><p class = \"executionText\">Unsuccessful command: there are no queries no process</p></div>";
            session.setAttribute("execute", execute);
        }
        // If none of the above, then the query must be an UPDATE
        else {
            if (userType.equals("client")) {
                execute = "<div class = \"executionContainerBad\"><p class = \"executionText\">SELECT is the only command allowed for client-user</p></div>";
            }
            else if (userType.equals("root") || userType.equals("dataentry")){
                try {
                    // Run the update query
                    execute = updateQuery(textBox, userType);
                    // Show the user what how many records was updated
                    session.setAttribute("execute", execute);
                }catch(SQLException e) {
                    // Show any errors to the user
                    execute = "<div class = \"executionContainerBad\"><p class = \"executionText\">" + e.getMessage() + "</p></div>";
                    e.printStackTrace();
                }
            }
            session.setAttribute("execute", execute);
        }

        // Keep the text inside the textBox
        if (!userType.equals("dataentry")){
            text = textBox;
            session.setAttribute("textBox", text);
        }
    }


    private String selectQuery(String input) throws SQLException {
        StringBuilder result = new StringBuilder();
        // Execute and process the query
        ResultSet table = statement.executeQuery(input);
        ResultSetMetaData metaData = table.getMetaData();
        // Count the table columns to insert
        int numColumns = metaData.getColumnCount();

        // Create a table
        result.append("<table class='table-sortable'>").append("<thead class='thead-dark'><tr>");

        // Insert all the column headers
        for (int i = 1; i <= numColumns; i++) {
            result.append("<th scope='col'>").append(metaData.getColumnName(i).toUpperCase()).append("</th>");
        }
        // Close the headers
        result.append("</tr></thead>");

        // Populate the rows of the table
        result.append("<tbody>");
        while (table.next()) {
            result.append("<tr>");
            for (int i = 1; i <= numColumns; i++) {
                if (i == 1) result.append("<td scope'row'>").append(table.getString(i)).append("</th>");
                else result.append("<td>").append(table.getString(i)).append("</th>");
            }
            result.append("</tr>");
        }
        // Close the table
        result.append("</tbody>").append("</table>");
        return result.toString();
    }

    private String updateQuery(String input, String userType) throws SQLException {
        StringBuilder result = new StringBuilder();
        int numRowsUpdated = 0;

        // Removes the bug that stops any insert queries for the table shipments
        //statement.execute("SET FOREIGN_KEY_CHECKS = 0;");
        // Store the count of shipments greater or equal than 100 here
        int numShipmentBefore = getNumShipments();

        if (!userType.equals("dataentry")) {
            // Create a temporary table to keep track of the number of shipments
            statement.executeUpdate("CREATE TABLE tempTable LIKE shipments");
            // Copy all the contents of shipments to the tempTable
            statement.executeUpdate("INSERT INTO tempTable SELECT * FROM shipments");
        }


        // Execute the query
        result.append("<div class = \"executionContainer\"><p class = \"executionText\">");
        numRowsUpdated = statement.executeUpdate(input);
        result.append("The statement executed succesfully.</br>").append(numRowsUpdated).append(" row(s) affected");

        // Count the number of shipment with quantity greater or equal than 100 after executing the query
        int numShipmentAfter = getNumShipments();

        // Update the status of the suppliers if the shipment quantity exceeds 100
        if (numShipmentBefore < numShipmentAfter && !userType.equals("dataentry")) {
            int numRowsAffected = statement.executeUpdate("UPDATE suppliers SET status = status + 5 WHERE snum IN " +
                    "(SELECT DISTINCT snum FROM shipments LEFT JOIN tempTable USING (snum, pnum, jnum, quantity) " +
                    "WHERE tempTable.snum IS NULL)");

            result.append("</br>Business Logic Detected! - Updating Supplier Status");
            result.append("</br>Business Logic Updated ").append(numRowsAffected).append(" Supplier(s) status marks");
        }

        if (!userType.equals("dataentry")){
            // Delete the temporary table
            statement.executeUpdate("DROP TABLE tempTable");
        }

        result.append("</p>");
        return result.toString();
    }

    private int getNumShipments() throws SQLException {
        // Count the number of shipment with quantity greater or equal than 100
        ResultSet before = statement.executeQuery("SELECT COUNT(*) FROM shipments WHERE quantity >= 100");
        before.next();
        // Store the count of shipments greater or equal than 100 here
        return before.getInt(1);
    }
}
