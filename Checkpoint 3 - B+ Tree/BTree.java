import java.util.ArrayList;
import java.util.List;

/**
 * B+Tree Structure
 * Key - StudentId
 * Leaf Node should contain [ key,recordId ]
 */
class BTree {

    /**
     * Pointer to the root node.
     */
    private BTreeNode root;
    /**
     * Number of key-value pairs allowed in the tree/the minimum degree of B+Tree
     **/
    private int t;

    BTree(int t) {
        this.root = null;
        this.t = t;
    }

    /**
     * Helper method for recursion for the search method
     * @param node current node
     * @param studentId the studentId we are searching for
     * @return recordId if found, otherwise return -1
     */
    private long searchHelper(BTreeNode node, long studentId) {
        // Iterate through each key in BTree node, or stop when we find key greater than studentId
        int idx = 0;
        while (idx < node.n) {
            if (node.keys[idx] == studentId) return node.values[idx]; // Found, return recordId
            if (node.keys[idx] < studentId) idx++; // If key is less than studentId increment idx
            else break; // Otherwise we have found the idx for the child
        }
        if (node.leaf) return -1; // If we have not returned yet and this node is a leaf return -1
        else return searchHelper(node.children[idx], studentId); // Otherwise recurse down the child
    }

    long search(long studentId) {
        /**
         * TODO:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in the table and return -1.
         */
        BTreeNode root = this.root; // Start with root node
        long result = searchHelper(root, studentId); // Call helper method for recursion
        if (result == -1) {
            // Print not found message
            System.out.println("The given studentId has not been found in the table.");
        }
        return result; // Return recordId or -1 if not found
    }

    BTree insert(Student student) {
        /**
         * TODO:
         * Implement this function to insert in the B+Tree.
         * Also, insert in student.csv after inserting in B+Tree.
         */
        return this;
    }

    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
        return true;
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();

        /**
         * TODO:
         * Implement this function to print the B+Tree.
         * Return a list of recordIDs from left to right of leaf nodes.
         *
         */
        return listOfRecordID;
    }
}
