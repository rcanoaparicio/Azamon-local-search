package localsearchclasses;

import aima.search.framework.GoalTest;

public class IsSolution implements GoalTest {
    public boolean isGoalState(Object state) {
        return false; //Local search doesn't have a known goal state, simply carry out all iterations.
    }
}
