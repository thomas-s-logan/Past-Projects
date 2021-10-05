/* 

The following is an example of a project using Java and MySQL that was done for a database class.

The program will:
-Create the required connection with the database.
-Create all the required tables from the assignment.
-Load data into the newly created tables from 4 external files "assignmentInfo.dat", 
 "departmentInfo.dat", "employeeInfo.dat", and "projectInfo.dat" (not contained in this repo)
-Run the 4 required queries on the tables after they have been filled.
-Output the data to an external file the program creates called "results.txt"

All the necessary commands are contained within this file and will
auto complete when the file is ran. There is no need for extra input from the user.

*/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;


public class MySQLProgram {

    public static void main(String[] args) {
        try {
            // load the driver
            Class.forName("com.mysql.jdbc.Driver");

        } catch (Exception ex) {
            System.out.println("Class not found exception: " + ex.getMessage());
        }

        Connection conn = null;
        try {
            // get the connection
            conn = DriverManager.getConnection(
                   "jdbc:mysql://cs1.utm.edu:3306/?useSSL=false", "thosloga", "100%Army");

            Statement stmt = null;
            ResultSet rs = null;

	// MAKE THE PROGRAM OUTPUT TO FILE INSTEAD OF SCREEN.
	try {
	    // Set parameters to output to file.
	    File file = new File("results.txt");
	    FileOutputStream fos = new FileOutputStream(file);
	    PrintStream ps = new PrintStream(fos);
	    System.setOut(ps);
	}

	catch(FileNotFoundException fnfe){
		System.out.println(fnfe);
	}

            // create a statement
            stmt = conn.createStatement();
            stmt.executeUpdate("use thosloga");
            // executes the statement -Creates the tables.-
              stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Department("
              + "Dep_Code     varchar(3)      not null unique,"
              + "Dep_Name     varchar(30)     not null,"
              + "Phone_Ext    int(4)          not null,"
	      + "primary key (Dep_Code))"
              );

	      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Employee("
                + "Emp_ID       varchar(4)      not null unique,"
                + "Emp_Name     varchar(20)     not null,"
                + "Salary       int(6)          not null,"
                + "MGR_ID       varchar(4),"
                + "Dep_Code     varchar(3)     not null,"
		+ "primary key (Emp_ID))"
                );

	      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Project("
                + "Proj_Num     varchar(6)      not null unique,"
                + "Proj_Name    varchar(30)     not null,"
                + "Fee          int(7)          not null,"
                + "Due_Date     date            not null,"
		+ "primary key (Proj_Num))"
                );

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Assignment("
                + "Emp_ID       varchar(4)      not null,"
                + "Proj_Num     varchar(6)      not null,"
                + "Hours        int(4)          not null,"
		+ "primary key (Emp_ID, Proj_Num),"
		+ "foreign key (Emp_ID) references Employee (Emp_ID),"
		+ "foreign key (Proj_Num) references Project (Proj_Num))"
                );
	   
	    // Load all the data into the tables.
	    String loadCommand1 = "load data local infile 'departmentInfo.dat'"
		    		+ " into table Department fields terminated by \',\'"
				+ " lines terminated by \'\\n\';";
	    
	    String loadCommand2 = "load data local infile 'employeeInfo.dat'"
                                + " into table Employee fields terminated by \',\'"
                                + " lines terminated by \'\\n\';";

	    String loadCommand3 = "load data local infile 'projectInfo.dat'"
                                + " into table Project fields terminated by \',\'"
                                + " lines terminated by \'\\n\';";

	    String loadCommand4 = "load data local infile 'assignmentInfo.dat'"
                                + " into table Assignment fields terminated by \',\'"
                                + " lines terminated by \'\\n\';";

	    // Execute the load commands.
	    stmt.executeUpdate(loadCommand1);
	    stmt.executeUpdate(loadCommand2);
	    stmt.executeUpdate(loadCommand3);
	    stmt.executeUpdate(loadCommand4);

	    // -----START DATA QUERY TESTING.-----
	    // List the names (Proj_Name) and numbers (Proj_Num) of all projects which the
	    // Data Center department is working on.
	    System.out.println("Current Data Center Projects:");
	    rs = stmt.executeQuery("select distinct Proj_Name, p.Proj_Num from"
			    + " Project p, Employee e, Assignment a, Department d"
			    + " WHERE d.Dep_Name = 'Data Center'"
		    	    + " AND e.Dep_Code = d.Dep_Code"
			    + " AND e.Emp_ID = a.Emp_ID"
			    + " AND a.Proj_Num = p.Proj_Num");

	    // Display Results.
	    System.out.printf("\t%-25s %10s %n", "Project Name:", "Project #:");

	    while (rs.next()) {
                String name = rs.getString("Proj_Name");
                String number = rs.getString("Proj_Num");
                System.out.printf("\t%-25s %10s %n", name, number);
            }

	    System.out.println("");


	    // List the names and ids of those employees whose manager is from the 
	    // Customer Services department.
	    System.out.println("Employees Managed By The Customer Service Department:");
	    stmt.executeUpdate("set @Dcode = (select Dep_Code from Department where"
			    + " Dep_Name = 'Customer Services')");
            stmt.executeUpdate("set @Manager = (select Emp_ID from Employee where"
			    + " MGR_ID = '' AND Dep_Code = @Dcode)");
	    rs = stmt.executeQuery("select distinct Emp_Name, Emp_ID"
                            + " from Employee WHERE MGR_ID = @Manager");

	    System.out.println("\t" + "Name:" + "\t" + "ID:");

            while (rs.next()) {
                String name = rs.getString("Emp_Name");
                String id = rs.getString("Emp_ID");
                System.out.println("\t" + name + "\t" + id);
            }

	    System.out.println("");



            // List the names and ids of the employees who worked on more than one project.
	    System.out.println("Employees Who Have Worked On More Than One Project:");
            rs = stmt.executeQuery("select e.Emp_Name, e.Emp_ID, count(a.Emp_ID) as Total from"
                            + " Assignment a, Employee e"
                            + " WHERE a.Emp_ID = e.Emp_ID"
			    + " GROUP BY e.Emp_ID"
			    + " HAVING Total > 1");

	    System.out.println("\tName:" + "\tID:" + "\tProjects:");

            while (rs.next()) {
                String name = rs.getString("e.Emp_Name");
               String id = rs.getString("e.Emp_ID");
	       String projects = rs.getString("Total");
                System.out.println("\t" + name + "\t" + id + "\t" + projects);
            }

	    System.out.println("");

			    
            // Increase  the salaries by $500 for those employees who worked more than 
	    // 15 hours on assigned projects. Once completed, display the content of the 
	    // Employee table.
            System.out.println("Employees Eligible For $500 Bonus (Over 15 Hours On Projects)");
	    stmt.executeUpdate("create temporary table if not exists 15_Plus"
			    + " select Emp_ID, sum(Hours) as total"
			    + " from Assignment group by Emp_ID HAVING total > 15");
	    rs = stmt.executeQuery("select t.Emp_ID, t.total, e.Emp_Name FROM"
			    + " 15_Plus t, Employee e"
			    + " WHERE e.Emp_ID = t.Emp_ID");

	    // Display the employees eligible for raises.
	    System.out.println("\tID:" + "\tName:" + "\tHours:");
	    while (rs.next())	{
		    String empID = rs.getString("t.Emp_ID");
		    String name = rs.getString("e.Emp_Name");
		    String hours = rs.getString("t.total");
		    System.out.println("\t" + empID + "\t" + name + "\t" + hours);
	    }

	    System.out.println("");

	    // Increase the employees identified in 15_Plus salaries by $500.
	    stmt.executeUpdate("update Employee"
			    + " set Salary = Salary + 500"
			    + " where Emp_ID in (select 15_Plus.Emp_ID from 15_Plus)");

	    //Show the updated Employee table.
	    System.out.println("Employee Table After Bonuses.");
	    System.out.println("\t" + "ID:" + "\t" + "Name:" + "\t" + "Salary:"
			    + "\t" + "  Manager:" + "  " + "Dept Code:");
	    rs = stmt.executeQuery("select * from Employee");

	    while (rs.next()) {
                String empID = rs.getString("Emp_ID");
                String name = rs.getString("Emp_Name");
                String salary = rs.getString("Salary");
                String mgrID = rs.getString("MGR_ID");
                String depCode = rs.getString("Dep_Code");
                System.out.println("\t" + empID + "\t" + name + "\t" + salary + "\t  "
                         + mgrID + "\t    " + depCode);
            }


        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }

}
