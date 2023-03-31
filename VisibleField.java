// Name: Ziyue Wei
// USC NetID: ziyuewei
// CS 455 PA3
// Spring 2023


/**
  VisibleField class
  This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
  user can see about the minefield). Client can call getStatus(row, col) for any square.
  It actually has data about the whole current state of the game, including  
  the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
  It also has mutators related to actions the player could do (resetGameDisplay(), cycleGuess(), uncover()),
  and changes the game state accordingly.
  
  It, along with the MineField (accessible in mineField instance variable), forms
  the Model for the game application, whereas GameBoardPanel is the View and Controller in the MVC design pattern.
  It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from 
  outside this class via the getMineField accessor.  
 */
public class VisibleField {
   // ----------------------------------------------------------   
   // The following public constants (plus numbers mentioned in comments below) are the possible states of one
   // location (a "square") in the visible field (all are values that can be returned by public method 
   // getStatus(row, col)).
   
   // The following are the covered states (all negative values):
   public static final int COVERED = -1;   // initial value of all squares
   public static final int MINE_GUESS = -2;
   public static final int QUESTION = -3;

   // The following are the uncovered states (all non-negative values):

   // values in the range [0,8] corresponds to number of mines adjacent to this opened square
   
   public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
   public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
   public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
   // ----------------------------------------------------------   
  
   // <put instance variables here>
   private MineField mineField; // initial MineField used for this VisibleField
   private int[][] visibleField;
   private int numGuess;
   private boolean isWin;
   private boolean isLose;

   /**
      Create a visible field that has the given underlying mineField.
      The initial state will have all the locations covered, no mines guessed, and the game
      not over.
      @param mineField  the minefield to use for this VisibleField
    */
   public VisibleField(MineField mineField) {
      this.mineField = mineField;
      visibleField = new int[mineField.numRows()][mineField.numCols()];
      // make all the locations covered
      for (int i=0; i<mineField.numRows(); i++) {
         for (int j=0; j<mineField.numCols(); j++) {
            visibleField[i][j] = COVERED;
         }
      }
      numGuess = 0;
      isWin = false;
      isLose = false;
   }
   
   
   /**
      Reset the object to its initial state (see constructor comments), using the same underlying
      MineField. 
   */     
   public void resetGameDisplay() {
      // reset all the locations covered
      for (int i=0; i<mineField.numRows(); i++) {
         for (int j=0; j<mineField.numCols(); j++) {
            visibleField[i][j] = COVERED;
         }
      }
      // reset no mines guessed, and the game not over
      numGuess = 0;
      isWin = false;
      isLose = false;
   }
  
   
   /**
      Returns a reference to the mineField that this VisibleField "covers"
      @return the minefield
    */
   public MineField getMineField() {return mineField;}
   
   
   /**
      Returns the visible status of the square indicated.
      @param row  row of the square
      @param col  col of the square
      @return the status of the square at location (row, col).  See the public constants at the beginning of the class
      for the possible values that may be returned, and their meanings.
      PRE: getMineField().inRange(row, col)
    */
   public int getStatus(int row, int col) {return visibleField[row][col];}

   
   /**
      Returns the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
      or not.  Just gives the user an indication of how many more mines the user might want to guess.  This value will
      be negative if they have guessed more than the number of mines in the minefield.     
      @return the number of mines left to guess.
    */
   public int numMinesLeft() {
      return mineField.numMines()-numGuess;
   }
 
   
   /**
      Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
      changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
      changes it to COVERED again; call on an uncovered square has no effect.  
      @param row  row of the square
      @param col  col of the square
      PRE: getMineField().inRange(row, col)
    */
   public void cycleGuess(int row, int col) {
      switch (visibleField[row][col]) {
         // if COVERED, change to MINE_GUESS, and add 1 guess
         case COVERED:
            visibleField[row][col] = MINE_GUESS;
            numGuess++;
            break;
         // if MINE_GUESS, change to QUESTION, and minus 1 guess
         case MINE_GUESS:
            visibleField[row][col] = QUESTION;
            numGuess--;
            break;
         // if QUESTION, change to COVERED
         case QUESTION:
            visibleField[row][col] = COVERED;
            break;
         // call on an uncovered square has no effect
         default:
            break;
      }
   }

   
   /**
      Uncovers this square and returns false iff you uncover a mine here.
      If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in 
      the neighboring area that are also not next to any mines, possibly uncovering a large region.
      Any mine-adjacent squares you reach will also be uncovered, and form 
      (possibly along with parts of the edge of the whole field) the boundary of this region.
      Does not uncover, or keep searching through, squares that have the status MINE_GUESS. 
      Note: this action may cause the game to end: either in a win (opened all the non-mine squares)
      or a loss (opened a mine).
      @param row  of the square
      @param col  of the square
      @return false   iff you uncover a mine at (row, col)
      PRE: getMineField().inRange(row, col)
    */
   public boolean uncover(int row, int col) {
      if (mineField.hasMine(row,col)) { // Uncovers this square and returns false iff uncover a mine here
         visibleField[row][col] = EXPLODED_MINE;
         isLose = true;
         updateStatus();
         return false;
      } else {
         // Uncover open area iff no adjacent mines recursively
         findOpenArea(row, col);
         isWin = isWin();
         updateStatus();
         return true;
      }
   }
 
   
   /**
      Returns whether the game is over.
      (Note: This is not a mutator.)
      @return whether game has ended
    */
   public boolean isGameOver() {
      return isWin || isLose;
   }
 
   
   /**
      Returns whether this square has been uncovered.  (i.e., is in any one of the uncovered states, 
      vs. any one of the covered states).
      @param row of the square
      @param col of the square
      @return whether the square is uncovered
      PRE: getMineField().inRange(row, col)
    */
   public boolean isUncovered(int row, int col) {
      // all non-negative values are the uncovered states
      return visibleField[row][col]>=0;
   }

   // <put private methods here>
   /**
      Finding an open area with no mines in it.
      If the square wasn't a mine or adjacent to a mine it also uncovers
      all the squares in the neighboring area that are also not next to any mines,
      possibly uncovering a large region. Any mine-adjacent squares you reach will
      also be uncovered, and form (possibly along with parts of the edge of the whole field)
      the boundary of this region.
      Does not uncover, or keep searching through, squares that have the status MINE_GUESS.
      @param row  of the square
      @param col  of the square
      PRE: getMineField().inRange(row, col)
    */
   private void findOpenArea(int row, int col) {
      // if the square is out of range, do not uncover or keep searching
      if (!mineField.inRange(row,col)) {return;}
      // if the square has been uncovered, do not keep searching
      if (isUncovered(row, col)) {return;}
      // if the square status is MINE_GUESS, do not uncover or keep searching
      if (visibleField[row][col]==MINE_GUESS) {return;}
      // for the square of COVERED and QUESTION
      // if there exists mines adjacent to the square(row, col), update the square status = the number of mines adjacent
      if (mineField.numAdjacentMines(row,col)>0) {
         visibleField[row][col] = mineField.numAdjacentMines(row, col);
         return;
      } else { // there is 0 mine adjacent to the square (row, col)
         // update the square status
         visibleField[row][col] = mineField.numAdjacentMines(row, col);
         //recursively uncover every square around
         findOpenArea(row-1, col-1);
         findOpenArea(row-1, col);
         findOpenArea(row-1, col+1);
         findOpenArea(row, col-1);
         findOpenArea(row, col+1);
         findOpenArea(row+1, col-1);
         findOpenArea(row+1, col);
         findOpenArea(row+1, col+1);
      }

   }

   /**
      Judge iff all the non-mine locations are opened.
    */
   private boolean isWin() {
      // check through every square making sure all the non-mine locations are opened
      for (int i=0; i<mineField.numRows(); i++) {
         for (int j=0; j<mineField.numCols(); j++) {
            if (!mineField.hasMine(i,j)) {
               if (!isUncovered(i,j)) {
                  return false;
               }
            }
         }
      }
      return true;
   }

   /**
    Update the status of visibleField when the game is over.
    */
   private void updateStatus() {
      if (isLose==true) {
         for (int i=0; i<mineField.numRows(); i++) {
            for (int j=0; j<mineField.numCols(); j++) {
               if (!isUncovered(i,j)) {
                  // if the square doesn't have a mine, but mark it as MINE_GUESS,
                  // change the status of the square to be INCORRECT_GUESS
                  if (visibleField[i][j]==MINE_GUESS) {
                     if (!mineField.hasMine(i,j)) {visibleField[i][j] = INCORRECT_GUESS;}
                  }
                  // if the square has a mine, but didn't mark is as MINE_GUESS,
                  // change the status of the square to be MINE
                  else if (mineField.hasMine(i,j)) {visibleField[i][j] = MINE;}
               }
            }
         }
      }
      if (isWin==true) {
         // if the square has a mine, but we did not mark it as MINE_GUESS, mark the square as MINE_GUESS
         for (int i=0; i<mineField.numRows(); i++) {
            for (int j=0; j<mineField.numCols(); j++) {
               if (!isUncovered(i,j)) {
                  if (mineField.hasMine(i,j)) {visibleField[i][j] = MINE_GUESS;}
               }
            }
         }
      }
   }
}
