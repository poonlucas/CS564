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

    private void insertNonFull(BTreeNode node, Student student) {
        int i = node.n - 1;

        if (node.leaf) {
            while (i >= 0 && student.studentId < node.keys[i]) {
                node.keys[i + 1] = node.keys[i];
                node.values[i + 1] = node.values[i];
                i--;
            }

            node.keys[i + 1] = student.studentId;
            node.values[i + 1] = student.recordId;
            node.n++;

            if (node.n == 2 * t) {
                split(node);
            }
        } else {
            while (i >= 0 && student.studentId < node.keys[i]) {
                i--;
            }
            i++;

            if (node.children[i].n == 2 * t - 1) {
                split(node.children[i]);

                if (student.studentId > node.keys[i]) {
                    i++;
                }
            }
            insertNonFull(node.children[i], student);
        }
    }

    private void split(BTreeNode node) {
        int mid = t - 1;
        BTreeNode newNode = new BTreeNode(t, node.leaf);

        for (int j = 0; j < t - 1; j++) {
            newNode.keys[j] = node.keys[j + t];
            if (node.leaf) {
                newNode.values[j] = node.values[j + t];
            }
        }

        if (!node.leaf) {
            for (int j = 0; j < t; j++) {
                newNode.children[j] = node.children[j + t];
            }
        }

        newNode.n = t - 1;
        node.n = t - 1;

        if (node.leaf) {
            newNode.next = node.next;
            node.next = newNode;
        }

        if (node == root) {
            BTreeNode newRoot = new BTreeNode(t, false);
            newRoot.keys[0] = node.keys[mid];
            newRoot.children[0] = node;
            newRoot.children[1] = newNode;
            newRoot.n = 1;
            root = newRoot;
        } else {
            BTreeNode parent = findParent(root, node);
            int i = parent.n - 1;
            while (i >= 0 && node.keys[mid] < parent.keys[i]) {
                parent.keys[i + 1] = parent.keys[i];
                parent.children[i + 2] = parent.children[i + 1];
                i--;
            }
            parent.keys[i + 1] = node.keys[mid];
            parent.children[i + 2] = newNode;
            parent.n++;
        }
    }

    private BTreeNode findParent(BTreeNode current, BTreeNode child) {
        if (current == null || current.leaf) {
            return null;
        }

        for (int i = 0; i <= current.n; i++) {
            if (current.children[i] == child) {
                return current;
            }
            BTreeNode parent = findParent(current.children[i], child);
            if (parent != null) {
                return parent;
            }
        }

        return null;
    }


    BTree insert(Student student) {
        /**
         * TODO:
         * Implement this function to insert in the B+Tree.
         * Also, insert in student.csv after inserting in B+Tree.
         */

         if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = student.studentId;
            root.values[0] = student.recordId;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1) {
                BTreeNode newRoot = new BTreeNode(t, false);
                newRoot.children[0] = root;
                split(root);
                root = newRoot;
            }
            insertNonFull(root, student);
        }
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
