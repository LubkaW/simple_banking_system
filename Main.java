package banking;


import java.util.*;
import java.lang.*;
import java.sql.*;



public class Main {


    public static void main(String[] args)
    {

        if(args.length < 2) System.out.println("Nezadali nameFile");
        Database database = new Database(args[1]);
        database.createTable();


        Scanner sc = new Scanner(System.in);
        boolean cont2;


        while(true)
        {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            String answer = sc.nextLine();

            switch(answer)
            {
                case "1":
                    Account ac = new Account();
                    System.out.println("Your card has been created");
                    System.out.println("Your card number:");
                    System.out.println(ac.getCardNumber());
                    System.out.println("Your card PIN:");
                    System.out.println(ac.getPin());
                    database.insertToTable(ac.getCardNumber(), ac.getPin(), (int)ac.getBalance());
                    break;

                case "2":
                    System.out.println("Enter your card number:");
                    String numero = sc.nextLine();
                    System.out.println("Enter your PIN:");
                    String pinos = sc.nextLine();
                    System.out.println(numero +"   "+pinos);
                    Account findedAccount = database.selectFromTable(numero, pinos);
                   // System.out.println(findedAccount.getCardNumber() +"   "+findedAccount.getPin() );

                            if(findedAccount != null){


                                System.out.println("You have successfully logged in!");

                                cont2 = true;
                                while(cont2){
                                    System.out.println("1. Balance");
                                    System.out.println("2. Add income");
                                    System.out.println("3. Do transfer");
                                    System.out.println("4. Close account");
                                    System.out.println("5. Log out");
                                    System.out.println("0. Exit");
                                    String answer2 = sc.nextLine();

                                    switch(answer2){
                                        case "1":
                                            System.out.println((int)findedAccount.getBalance());
                                            break;

                                        case "2":
                                            System.out.println("How much do you want to deposit:");
                                            try{
                                                findedAccount.addBalance(Integer.parseInt(sc.nextLine()));
                                            }catch(NumberFormatException e){
                                                System.out.println(e.getMessage());
                                            }
                                            database.updateBalance(findedAccount);

                                            break;

                                        case "3":
                                            System.out.println("Enter receiver number:");
                                            String recNumber = sc.nextLine();
                                            if(findedAccount.getCardNumber().equals(recNumber)){
                                                System.out.println("You can't transfer money to the same account!");
                                                break;
                                            }

                                            if(findedAccount.checkLuhn(recNumber)){
                                               System.out.println("Passed Luhn Alghorithm");
                                            }else{
                                                System.out.println("Probably you made mistake in the card number. Please try again!");
                                                break;
                                            }

                                            if(!database.checkDatabaseNumer(recNumber)){
                                                break;
                                            }

                                            int recMoney = 0;
                                            System.out.println("Enter money to transfer: ");

                                            try{
                                                recMoney = Integer.parseInt(sc.nextLine());
                                            }catch(NumberFormatException e){
                                                System.out.println(e.getMessage());
                                            }

                                            if((int)findedAccount.getBalance() < recMoney){
                                                System.out.println("Not enough money!");
                                                break;
                                            }

                                            findedAccount.transferBalance(recMoney);
                                            database.updateBalance(findedAccount);
                                            database.transferMoney(recNumber,recMoney);
                                            break;

                                        case "4":
                                            database.closeAccount(findedAccount);
                                            findedAccount=null;

                                            break;

                                        case "5":
                                            cont2 = false;
                                            break;
                                        case "0":
                                            System.out.println("bye!");
                                            return;
                                    }

                                }

                                System.out.println("You have successfully logged out!");

                            }else{
                                System.out.println("Wrong Combination of PIN and Card Number");
                            }




                    break;
                    case "0":
                        System.out.println("bye!");
                        return;
            }
        }

    }
}









class Database{
    String DBname;

    public Database(String DBname){

        this.DBname = DBname;

    }

    public Connection connect(){
        String url = "jdbc:sqlite:"+DBname;
        Connection conn = null;

        try{
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");

        }catch(SQLException e){

            System.out.println(e.getMessage());

        }

        return conn;

    }


    public void createTable(){

        String sql ="CREATE TABLE IF NOT EXISTS card (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	number TEXT,\n"
                + "	pin TEXT,\n"
                + " balance INTEGER DEFAULT 0\n"
                + ");";

        try{

            Connection conn = connect();
            Statement stm = conn.createStatement();
            stm.execute(sql);
            conn.close();

        }catch(SQLException e){

            System.out.println(e.getMessage());

        }



    }

    public void transferMoney(String recNumber, int recMoney){

        String sql = "UPDATE card SET balance = balance + ?"
                +    " WHERE number = ? ;";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1,recMoney);
            pstmt.setString(2, recNumber);

            pstmt.executeUpdate();
            System.out.println("Successful update");

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    public void closeAccount(Account account){
        String sql = "DELETE FROM card WHERE number = ? ;";

        try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            System.out.println(account.getCardNumber());
            pstmt.setString(1, account.getCardNumber());
            pstmt.executeUpdate();
            System.out.println("Account has been closed");

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    public void updateBalance(Account account){
        String sql = "UPDATE card SET balance = ?"
                +    " WHERE number = ? ;";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1,(int)account.getBalance());
            pstmt.setString(2, account.getCardNumber());

            pstmt.executeUpdate();
            System.out.println("Successful update");

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }


    }

    public boolean checkDatabaseNumer(String recNumber){
        String sql = "SELECT number FROM card WHERE number = " +recNumber +";";

        try (Connection conn = connect();
            Statement pstmt = conn.createStatement();
             ResultSet es = pstmt.executeQuery(sql)){


            if(es.getString("number")==null){
                System.out.println("Such a card does not exist.");
                return false;
            }


        }catch (SQLException e){
            System.out.println(e.getMessage());

        }
        return true;
    }

    public Account selectFromTable(String numero, String pin){

        String sql = "SELECT number,pin,balance "
                +    "FROM card"
                +    " WHERE number = " + numero
                +    " AND pin = " +pin+";";

        try(Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet es = stmt.executeQuery(sql)){

            if(es.getString("number") == null){
                System.out.println("Card does not exist!");
                return null;
            }


            return new Account(es.getString("number"),es.getString("pin"),es.getInt("balance"));


        }catch(SQLException e){
            System.out.println(e.getMessage());
            System.out.println("Karta neexistuje nebo chyba");
            return null;
        }




    }




    public void insertToTable(String number, String pin, int balance){
            String sql = "INSERT INTO card(number,pin,balance) VALUES (?,?,?);";

            try{
                Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1,number);
                pstmt.setString(2,pin);
                pstmt.setInt(3,balance);
                pstmt.executeUpdate();
                conn.close();
            }catch (SQLException e){
                System.out.println(e.getMessage());
            }

    }


} // Konec Database class


  class Account{

  private String cardNumber;
  private String pin;
  private double balance = 0;

  public Account(String cardNumber, String pin, double balance){
      this.cardNumber = cardNumber;
      this.pin = pin;
      this.balance = balance;
  }


  StringBuilder sb = new StringBuilder();
  Random rnd = new Random();
  public Account()
  {
      sb.append("400000");
      for(int i = 0; i < 9; i++) {
          sb.append(rnd.nextInt(10));
      }
      createCheckSum();
      sb.setLength(0);
      for(int i =0; i < 4; i++){
          sb.append(rnd.nextInt(10));
      }
      pin = sb.toString();

  }

  public String getCardNumber(){
      return cardNumber;
  }

  public String getPin(){
      return  pin;
  }

  public double getBalance() {return balance;}

  public void addBalance(int money){
      this.balance += money;
  }

  public void transferBalance(int money){
          this.balance =- money;
  }

  public  boolean checkLuhn(String num){
      int[] nums = new int[16];
      int sum =0;
      for(int i = 0; i<16; i++){
          nums[i] = Character.getNumericValue(num.charAt(i));
      }

      for(int i = 0; i < 16; i ++)
      {
          if( i % 2 == 0) {
              if (nums[i] * 2 > 9) {
                  sum += nums[i]*2 - 9;
              } else {
                  sum += nums[i]*2;
              }
          }
          else{
              sum += nums[i];
          }

      }

      return sum % 10 == 0;
  }

  private void createCheckSum(){
      int[] nums = new int[15];
      String[] stringNums = sb.toString().split("");
      int sum = 0;
      for(int i = 0; i < 15; i++){
          nums[i] = Integer.parseInt(stringNums[i]);
      }

      for(int i = 0; i < 15; i ++)
      {
          if( i % 2 == 0) {
              if (nums[i] * 2 > 9) {
                  sum += nums[i]*2 - 9;
              } else {
                  sum += nums[i]*2;
              }
          }
          else{
              sum += nums[i];
          }

      }
      if(sum%10 == 0){
          sb.append(0);
          cardNumber = sb.toString();
          return;
      }
      sb.append(Math.abs((sum%10)-10));
      cardNumber = sb.toString();
  }
}

