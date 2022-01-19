package localsearchclasses;

import aima.search.framework.SuccessorFunction;

import java.util.List;

public class AnnealingSuccessors implements SuccessorFunction {
    public List getSuccessors(Object state) {
        Company company = (Company) state;
        return company.randomSuccessor(); //Must be a NEW INSTANCE of Company
    }
}