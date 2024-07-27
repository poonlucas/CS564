import java.util.ArrayList;
import java.util.Arrays;
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
     * @return leaf node of where the studentId would be if it was in btree
     */
    private BTreeNode searchHelper(BTreeNode node, long studentId) {
        // If node is a leaf, return node
        if (node.leaf) {
            return node;
        }
        // studentID less than node's key at index 0, recurse down left most child
        if (studentId < node.keys[0]) {
            return searchHelper(node.children[0], studentId);
        }
        // studentID more than node's key at index[n-1], recurse down right most child
        if (studentId > node.keys[node.n - 1]) {
            return searchHelper(node.children[node.n], studentId);
        }
        // Find i s.t. key[i] <= studentId < key[i+1] and recurse down child at index i
        for (int i = 0; i < node.n - 1; i++) {
            if (node.keys[i] <= studentId && studentId < node.keys[i + 1]) {
                return searchHelper(node.children[i], studentId);
            }
        }
        return null;
    }

    long search(long studentId) {
        /**
         * TODO:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in the table and return -1.
         */
        // If root is null, btree is empty hence return not found.
        if (this.root == null) {
            System.out.println("The given studentId has not been found in the table.");
            return -1;
        }
        // Get leaf node where the studentID would be in if it was in btree
        BTreeNode leaf = searchHelper(this.root, studentId);
        // Iterate through leaf node to see if studentID exists
        // Probably can do binary search to get O(logt) time instead of linear O(n)
        for (int i = 0; i < leaf.n; i++) {
            // If key at index i is equal to studentID, return the recordID at index i
            if (leaf.keys[i] == studentId) {
                return leaf.values[i];
            }
        }
        // studentID not found, return -1
        System.out.println("The given studentId has not been found in the table.");
        return -1;
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

    /**
     * Helper method deletion for given studentId
     * @param parent parent node
     * @param node current node
     * @param studentId studentId to delete
     */
    private DeleteResult deleteHelper(BTreeNode parent, BTreeNode node, long studentId, BTreeNode oldChildEntry) {
        // If pointer is not a leaf node, recurse until we find leaf where studentId belongs
        if (!node.leaf) {
            // studentID less than node's key at index 0, recurse down left most child
            if (studentId < node.keys[0]) {
                oldChildEntry = deleteHelper(node, node.children[0], studentId, oldChildEntry);
            }
            // studentID more than node's key at index[n-1], recurse down right most child
            if (studentId > node.keys[node.n - 1]) {
                oldChildEntry = deleteHelper(node, node.children[node.n], studentId, oldChildEntry);
            }
            // Find i s.t. key[i] <= studentId < key[i+1] and recurse down child at index i
            for (int i = 0; i < node.n - 1; i++) {
                if (node.keys[i] <= studentId && studentId < node.keys[i + 1]) {
                    oldChildEntry = deleteHelper(node, node.children[i], studentId, oldChildEntry);
                }
            }
            if (oldChildEntry == null) {
                return null;
            } else {
                // Find index of old child entry to be deleted
                int idx = 0;
                while (idx < node.n && !node.children[idx].equals(oldChildEntry)) {
                    idx++;
                }
                // Delete and shift key and children in this node
                for (int i = idx; i < node.n - 1; i++) {
                    node.keys[i - 1] = node.keys[i];
                    node.children[i] = node.children[i + 1];
                }
                node.keys[node.n - 1] = 0;
                node.children[node.n] = null;
                node.n--;

                // If node has entries to spare, we are done
                if (node.n >= node.t || node.equals(this.root)) {
                    return null;
                }

                // Otherwise try to redistribute
                // Find index of current node in parent
                int currIndex = 0;
                // Iterate until we find the index of node in parent's children
                while (currIndex < parent.n && !parent.children[currIndex].equals(node)) {
                    currIndex++;
                }

                // Try right sibling, if it exists
                if (currIndex + 1 < parent.n) {
                    // Right sibling
                    BTreeNode rightSibling = parent.children[currIndex + 1];
                    // Check if we can redistribute, that is, if the total number of keys is greater or equal to 2 * degree
                    int total = (node.n + rightSibling.n);
                    if (total >= 2 * node.t) {
                        // Move first in right sibling into current node
                        node.keys[node.n] = rightSibling.keys[0];
                        node.children[node.n] = rightSibling.children[0];
                        node.n++;
                        // Now remove first key and child in right sibling
                        for (int i = 0; i < rightSibling.n - 1; i++) {
                            rightSibling.keys[i] = rightSibling.keys[i + 1];
                            rightSibling.children[i] = rightSibling.children[i + 1];
                        }
                        rightSibling.keys[rightSibling.n - 1] = 0;
                        rightSibling.children[rightSibling.n - 1] = null;
                        // Decrement n
                        rightSibling.n--;
                        // Update parent
                        parent.keys[currIndex] = rightSibling.keys[0];
                        // We are done
                        return null;
                    }
                }
                // Try left sibling, if it exists
                if (currIndex - 1 >= 0) {
                    // Left sibling
                    BTreeNode leftSibling = parent.children[currIndex - 1];
                    // Check if we can redistribute, that is, if the total number of keys is greater or equal to 2 * degree
                    int total = (node.n + leftSibling.n);
                    if (total >= 2 * node.t) {
                        // Move last in left sibling into first in current node
                        for (int i = node.n - 1; i > 0; i--) {
                            node.keys[i] = node.keys[i - 1];
                            node.children[i + 1] = node.children[i];
                        }
                        node.keys[0] = leftSibling.keys[leftSibling.n - 1];
                        node.children[0] = leftSibling.children[leftSibling.n - 1];
                        node.n++;
                        // Now remove last key and child in left sibling, decrement n for left sibling
                        leftSibling.keys[leftSibling.n - 1] = 0;
                        leftSibling.children[leftSibling.n - 1] = null;
                        leftSibling.n--;
                        // Update parent
                        parent.keys[currIndex] = node.keys[0];
                        // We are done
                        return null;
                    }
                }

                // Otherwise merge
                // Try right sibling, if it exists
                if (currIndex + 1 < parent.n) {
                    // Right sibling
                    BTreeNode rightSibling = parent.children[currIndex + 1];
                    // Pull splitting key from parent
                    node.keys[node.n] = parent.keys[node.n];
                    node.n++;
                    // Merge right sibling into current node
                    for (int i = 0; i < rightSibling.n - 1; i++) {
                        node.keys[node.n + i] = rightSibling.keys[i];
                        node.children[node.n + i] = rightSibling.children[i];
                    }
                    node.n += rightSibling.n;
                    node.children[node.n] = rightSibling.children[rightSibling.n];
                    // Adjust sibling pointers
                    node.next = rightSibling.next;
                    // Return right sibling as old child entry
                    return rightSibling;
                }

                // Try left sibling, if it exists
                if (currIndex - 1 >= 0) {
                    // Left sibling
                    BTreeNode leftSibling = parent.children[currIndex + 1];
                    // Pull splitting key from parent
                    leftSibling.keys[node.n] = parent.keys[node.n];
                    // Merge left sibling into current node
                    for (int i = 0; i < node.n - 1; i++) {
                        leftSibling.keys[leftSibling.n + i] = node.keys[i];
                        leftSibling.children[leftSibling.n + i] = node.children[i];
                    }
                    leftSibling.n += node.n;
                    leftSibling.children[leftSibling.n] = node.children[node.n];
                    // Adjust sibling pointers
                    leftSibling.next = node.next;
                    // Return current node as old child entry
                    return node;
                }
            }
        }

        // Otherwise, this node is a leaf pointer
        else {
            // Find index of studentID if it exists and delete
            int idx = 0;
            // Iterate through keys until found or n to find index of studentId if it exists
            while (idx < node.n && node.keys[idx] != studentId) {
                idx++;
            }
            // If idx is greater or equal to node.n then it does not exist hence return false
            if (idx >= node.n) {
                return null;
            }

            // Shift key value arrays and delete the element
            for (int i = idx; i < node.n; i++) {
                node.keys[idx] = node.keys[idx + 1];
                node.values[idx] = node.values[idx + 1];
            }
            // Decrement number of nodes in node
            node.n--;
            // TODO: csv update
            // If leaf node has entries to spare, we are done
            if (node.n >= node.t) {
                return null;
            }

            // Otherwise try to redistribute
            // Find index of current node in parent
            int currIndex = 0;
            // Iterate until we find the index of node in parent's children
            while (currIndex < parent.n && !parent.children[currIndex].equals(node)) {
                currIndex++;
            }
            // Try right sibling, if it exists
            if (currIndex + 1 < parent.n) {
                // Right sibling
                BTreeNode rightSibling = parent.children[currIndex + 1];
                // Check if we can redistribute, that is, if the total number of kv pairs is greater or equal to 2 * degree
                int total = (node.n + rightSibling.n);
                if (total >= 2 * node.t) {
                    // Move first in right sibling into current node
                    node.keys[node.n] = rightSibling.keys[0];
                    node.values[node.n] = rightSibling.values[0];
                    node.n++;
                    // Now remove first kv in right sibling
                    for (int i = 0; i < rightSibling.n - 1; i++) {
                        rightSibling.keys[i] = rightSibling.keys[i + 1];
                        rightSibling.values[i] = rightSibling.values[i + 1];
                    }
                    rightSibling.keys[rightSibling.n - 1] = 0;
                    rightSibling.values[rightSibling.n - 1] = 0;
                    // Decrement n
                    rightSibling.n--;
                    // Update parent
                    parent.keys[currIndex] = rightSibling.keys[0];
                    // We are done
                    return null;
                }
            }
            // Try left sibling, if it exists
            if (currIndex - 1 >= 0) {
                // Left sibling
                BTreeNode leftSibling = parent.children[currIndex - 1];
                // Check if we can redistribute, that is, if the total number of kv pairs is greater or equal to 2 * degree
                int total = (node.n + leftSibling.n);
                if (total >= 2 * node.t) {
                    // Move last in left sibling into first in current node
                    for (int i = node.n - 1; i > 0; i--) {
                        node.keys[i] = node.keys[i - 1];
                        node.values[i] = node.keys[i - 1];
                    }
                    node.keys[0] = leftSibling.keys[leftSibling.n - 1];
                    node.values[0] = leftSibling.values[leftSibling.n - 1];
                    node.n++;
                    // Now remove last kv in left sibling, decrement n for left sibling
                    leftSibling.keys[leftSibling.n - 1] = 0;
                    leftSibling.values[leftSibling.n - 1] = 0;
                    leftSibling.n--;
                    // Update parent
                    parent.keys[currIndex] = node.keys[0];
                    // We are done
                    return null;
                }
            }

            // Otherwise merge
            // Try right sibling, if it exists
            if (currIndex + 1 < parent.n) {
                // Right sibling
                BTreeNode rightSibling = parent.children[currIndex + 1];
                // Merge right sibling kv into current node kvs, sum the total kvs
                for (int i = 0; i < rightSibling.n - 1; i++) {
                    node.keys[node.n + i] = rightSibling.keys[i];
                    node.values[node.n + i] = rightSibling.values[i];
                }
                node.n += rightSibling.n;
                // Adjust sibling pointers
                node.next = rightSibling.next;
                // Return right sibling as old child entry
                return rightSibling;
            }

            // Try left sibling, if it exists
            if (currIndex - 1 >= 0) {
                // Left sibling
                BTreeNode leftSibling = parent.children[currIndex + 1];
                // Merge right sibling kv into current node kvs, sum the total kvs
                for (int i = 0; i < node.n - 1; i++) {
                    leftSibling.keys[leftSibling.n + i] = node.keys[i];
                    leftSibling.values[leftSibling.n + i] = node.values[i];
                }
                leftSibling.n += node.n;
                // Adjust sibling pointers
                leftSibling.next = node.next;
                // Return current node as old child entry
                return node;
            }
        }
        return null;
    }


    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
        // If btree is empty, return false
        if (this.root == null) {
            return false;
        }
        return deleteHelper(null, this.root, studentId, null);
    }

    public BTreeNode getLeftMostNode(BTreeNode node) {
        if (node.leaf) { // If node is leaf, return current node
            return node;
        }
        else { // If node is not leaf, recurse down left subtree
            return getLeftMostNode(node.children[0]);
        }
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();

        /**
         * TODO:
         * Implement this function to print the B+Tree.
         * Return a list of recordIDs from left to right of leaf nodes.
         *
         */
        // If root is null, btree is empty, hence return empty list
        if (this.root == null) {
            return listOfRecordID;
        }
        // Get leftmost leaf node
        BTreeNode curr = getLeftMostNode(this.root);
        // Iterate through each leaf node from left to right
        while (curr != null) {
            // Iterate through each recordID in leaf node and add to list of recordIDs
            for (int i = 0; i < curr.n; i++) {
                listOfRecordID.add(curr.values[i]);
            }
            // Set current node to next
            curr = curr.next;
        }

        return listOfRecordID;
    }
}
