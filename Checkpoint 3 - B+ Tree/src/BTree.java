import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * RecordID to Student Mapping
     */
    private Map<Long, Student> recordToStudentMap;

    BTree(int t) {
        this.root = null;
        this.t = t;
        this.recordToStudentMap = new HashMap<>();
    }

    private int findIndex(BTreeNode node, long studentId) {
        // studentID less than node's key at index 0, recurse down left most child
        if (studentId < node.keys[0]) {
            return 0;
        }
        // studentID more than node's key at index[n-1], recurse down right most child
        if (studentId > node.keys[node.n - 1]) {
            return node.n;
        }
        // Find i s.t. key[i] <= studentId < key[i+1] and recurse down child at index i
        for (int i = 0; i < node.n - 1; i++) {
            if (node.keys[i] <= studentId && studentId < node.keys[i + 1]) {
                return ++i;
            }
        }
        return -1;
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
        // Find index i s.t. K_i <= studentId < K_{i+1}
        int i = findIndex(node, studentId);
        return searchHelper(node.children[i], studentId);
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

    private static class NewChildEntry {
        BTreeNode newChildEntryPointer;
        long newChildEntry;

        public NewChildEntry(BTreeNode newChildEntryPointer, long newChildEntry) {
            this.newChildEntryPointer = newChildEntryPointer;
            this.newChildEntry = newChildEntry;
        }

        public BTreeNode getNewChildEntryPointer() {
            return newChildEntryPointer;
        }

        public long getNewChildEntry() {
            return newChildEntry;
        }
    }

    private NewChildEntry insertHelper(BTreeNode node, Student student) {
        // If pointer is not a leaf node, recurse until we find leaf where studentId belongs
        if (!node.leaf) {
            // Find index i s.t. K_i <= studentId < K_{i+1}
            int index = findIndex(node, student.studentId);
            // Recursively, insert entry
            NewChildEntry newChildEntry = insertHelper(node.children[index], student);
            // Usual case, did not split child
            if (newChildEntry == null) {
                return null;
            }
            // If node has space, put new child entry and return
            if (node.n < node.keys.length) {
                int insertIndex = findIndex(node, newChildEntry.getNewChildEntry());
                node.keys[insertIndex] = newChildEntry.getNewChildEntry();
                node.children[insertIndex + 1] = newChildEntry.getNewChildEntryPointer();
                node.n++;
                return null;
            }
            // Split
            int insertIndex = findIndex(node, student.studentId);
            BTreeNode newNode = new BTreeNode(this.t, false);

            int mid = this.t;
            for (int i = mid, j = 0; i < node.keys.length; i++, j++) {
                newNode.keys[j] = node.keys[i]; // Move last d keys to new node
                newNode.children[j] = node.children[i]; // Move last d+1 pointers to new node
                newNode.n++;
                node.keys[i] = 0;  // Clear the key in the old node
                node.children[i] = null;  // Clear the pointer in the old node
                node.n--;
            }
            newNode.children[newNode.n] = node.children[node.keys.length];
            node.children[node.keys.length] = null;

            // Set newChildEntry to guide searches between N and N2
            newChildEntry = new NewChildEntry(newNode, newNode.keys[0]);

            // Edge case, node is root
            if (node.equals(root)) {
                BTreeNode newRoot = new BTreeNode(this.t, false);
                newRoot.keys[0] = newNode.keys[0];
                newRoot.children[0] = node;
                newRoot.children[1] = newNode;
                newRoot.n = 1;
                this.root = newRoot;
                return null;
            }
            return newChildEntry;
        }

        int index = findIndex(node, student.studentId);
        // Node is a leaf node, insert and return
        if (node.n < node.keys.length) {
            for (int i = node.n; i > index; i--) {
                node.keys[i] = node.keys[i - 1];
                node.values[i] = node.values[i - 1];
            }
            node.keys[index] = student.studentId;
            node.values[index] = student.recordId;
            node.n++;
            return null;
        }
        // Not enough space, we split
        BTreeNode newNode = new BTreeNode(this.t, true);

        // If index is in first half
        if (index < this.t) {
            // Move d + 1 to second half
            for (int i = 0; i < this.t + 1; i++) {
                newNode.keys[i] = node.keys[this.t - 1 + i];
                newNode.values[i] = node.values[this.t - 1 + i];
                node.keys[this.t - 1 + i] = 0;
                node.values[this.t - 1 + i] = 0;
                newNode.n++;
                node.n--;
            }
            // Insert the student
            for (int i = node.n; i > index; i--) {
                node.keys[i] = node.keys[i - 1];
                node.values[i] = node.values[i - 1];
            }
            node.keys[index] = student.studentId;
            node.values[index] = student.recordId;
            node.n++;
        } else {
            // Move d to second half
            for (int i = 0; i < this.t; i++) {
                newNode.keys[i] = node.keys[this.t + i];
                newNode.values[i] = node.values[this.t + i];
                node.keys[this.t + i] = 0;
                node.values[this.t + i] = 0;
                newNode.n++;
                node.n--;
            }
            // Insert the student
            index = index - this.t;
            for (int i = newNode.n; i > index; i--) {
                node.keys[i] = node.keys[i - 1];
                node.values[i] = node.values[i - 1];
            }
            newNode.keys[index] = student.studentId;
            newNode.values[index] = student.recordId;
            newNode.n++;
        }

        newNode.next = node.next;
        node.next = newNode;

        // Edge case, node is root
        if (node.equals(root)) {
            BTreeNode newRoot = new BTreeNode(this.t, false);
            newRoot.keys[0] = newNode.keys[0];
            newRoot.children[0] = node;
            newRoot.children[1] = newNode;
            newRoot.n = 1;
            this.root = newRoot;
        }

        return new NewChildEntry(newNode, newNode.keys[0]);
    }

    BTree insert(Student student) {
        /**
         * TODO:
         * Implement this function to insert in the B+Tree.
         * Also, insert in student.csv after inserting in B+Tree.
         */

         if (this.root == null) {
            this.root = new BTreeNode(t, true);
            this.root.keys[0] = student.studentId;
            this.root.values[0] = student.recordId;
            this.root.n = 1;
            return this;
         }
         insertHelper(this.root, student);
         return this;
    }

    private static class OldChildEntry {
        BTreeNode oldChildEntry;

        public OldChildEntry() {
            this.oldChildEntry = null;
        }

        public BTreeNode getOldChildEntry() {
            return this.oldChildEntry;
        }

        public void setOldChildEntry(BTreeNode node) {
            this.oldChildEntry = node;
        }
    }

    /**
     * Helper method deletion for given studentId
     * @param parent parent node
     * @param node current node
     * @param studentId studentId to delete
     */
    private boolean deleteHelper(BTreeNode parent, BTreeNode node, long studentId, OldChildEntry oldChildEntry) {
        // If pointer is not a leaf node, recurse until we find leaf where studentId belongs
        if (!node.leaf) {
            // Find index i s.t. K_i <= studentId < K_{i+1}
            int index = findIndex(node, studentId);
            // Recursive delete
            boolean result = deleteHelper(node, node.children[index], studentId, oldChildEntry);
            // Usual case: Child not deleted
            if (oldChildEntry.getOldChildEntry() == null) {
                return result;
            }

            // We discarded child node
            // Find index of old child entry to be deleted
            int idx = 0;
            while (idx < node.n && !node.children[idx].equals(oldChildEntry.getOldChildEntry())) {
                idx++;
            }
            // Delete and shift key and children in this node
            for (int i = idx; i < node.n; i++) {
                node.keys[i - 1] = node.keys[i];
                node.children[i] = node.children[i + 1];
            }
            // Erase last element
            node.keys[node.n - 1] = 0;
            node.children[node.n] = null;
            // Decrement node.n
            node.n--;

            // Check minimum occupancy, or root, we are done
            if (node.n >= node.t || node.equals(this.root)) {
                oldChildEntry.setOldChildEntry(null); // Delete doesn't go further
                return result;
            }

            // Find index of current node in parent
            int currIndex = 0;
            // Iterate until we find the index of node in parent's children
            while (currIndex < parent.n && !parent.children[currIndex].equals(node)) {
                currIndex++;
            }

            // Siblings, null if not exist
            BTreeNode rightSibling = (currIndex + 1 < parent.n) ? parent.children[currIndex + 1] : null;
            BTreeNode leftSibling = (currIndex - 1 >= 0) ? parent.children[currIndex - 1] : null;

            // Try to redistribute
            if (rightSibling != null) {
                // Check if right sibling has extra entries
                int total = node.n + rightSibling.n;
                if (total / 2 >= this.t) {
                    // Redistribute evenly between right sibling and current node through parent
                    while (node.n < total / 2) {
                        // Current node gets parent key
                        node.keys[node.n] = parent.keys[currIndex];
                        node.n++;
                        // Parent node gets right sibling key
                        parent.keys[currIndex] = rightSibling.keys[0];
                        // Current node gets right sibling child
                        node.children[node.n + 1] = rightSibling.children[0];
                        // Shift right sibling
                        for (int i = 0; i < rightSibling.n - 1; i++) {
                            rightSibling.keys[i] = rightSibling.keys[i + 1];
                            rightSibling.children[i] = rightSibling.children[i + 1];
                        }
                        // One more time for children
                        rightSibling.children[rightSibling.n - 1] = rightSibling.children[rightSibling.n];
                        // Empty last element
                        rightSibling.keys[rightSibling.n - 1] = 0;
                        rightSibling.children[rightSibling.n] = null;
                        // Decrement right sibling n
                        rightSibling.n--;
                    }
                    oldChildEntry.setOldChildEntry(null); // Delete does not go further
                    return result;
                }
            }
            if (leftSibling != null) {
                // Check if left sibling has extra entries
                int total = node.n + leftSibling.n;
                if (total / 2 >= this.t) {
                    // Redistribute evenly between right sibling and current node through parent
                    while (node.n < total / 2) {
                        // Current node gets parent key
                        for (int i = node.n; i > 0; i--) {
                            node.keys[i] = node.keys[i - 1];
                        }
                        node.keys[0] = parent.keys[currIndex - 1];
                        node.n++;
                        // Parent node gets left sibling key
                        parent.keys[currIndex - 1] = leftSibling.keys[leftSibling.n - 1];
                        // Current node gets left sibling child
                        for (int i = node.n; i > 0; i--) {
                            node.children[i] = node.children[i - 1];
                        }
                        node.children[0] = leftSibling.children[leftSibling.n];
                        // Remove last key and child from left sibling
                        leftSibling.keys[leftSibling.n - 1] = 0;
                        leftSibling.children[leftSibling.n] = null;
                        leftSibling.n--;
                    }
                    oldChildEntry.setOldChildEntry(null); // Delete does not go further
                    return result;
                }
            }

            // Try to merge
            if (rightSibling != null) {
                oldChildEntry.setOldChildEntry(rightSibling);
                // Pull splitting key from parent down into current node
                node.keys[node.n] = parent.keys[currIndex];
                node.n++;
                // Move all entries in right sibling into current node
                for (int i = 0; i < rightSibling.n; i++) {
                    node.keys[node.n + i] = rightSibling.keys[i];
                    node.children[node.n + i] = rightSibling.children[i];
                }
                node.n += rightSibling.n;
                node.children[node.n] = rightSibling.children[rightSibling.n];
                // Right sibling will be automatically discarded when parent reference is removed
                return result;
            }
            if (leftSibling != null) {
                oldChildEntry.setOldChildEntry(node);
                // Pull splitting key from parent down into left sibling
                leftSibling.keys[leftSibling.n] = parent.keys[currIndex - 1];
                leftSibling.n++;
                // Move all entries in current node into left sibling
                for (int i = 0; i < node.n; i++) {
                    leftSibling.keys[leftSibling.n + i] = node.keys[i];
                    leftSibling.children[leftSibling.n + i] = node.children[i];
                }
                leftSibling.n += node.n;
                leftSibling.children[leftSibling.n] = node.children[node.n];
                // Current node will be automatically discarded when parent reference is removed
                return result;
            }
            return false;
        }

        // Otherwise, this node is a leaf
        int index = 0;
        while (index < node.n && node.keys[index] != studentId) {
            index++;
        }
        // If studentId not found in leaf node, return false
        if (index == node.n) {
            oldChildEntry.setOldChildEntry(null);
            return false;
        }

        // Delete entry
        for (int i = index; i < node.n - 1; i++) {
            node.keys[i] = node.keys[i + 1];
            node.values[i] = node.values[i + 1];
        }
        node.keys[node.n - 1] = 0;
        node.values[node.n - 1] = 0;
        node.n--;

        // If node has entries to spare or is root
        if (node.n >= this.t || node.equals(this.root)) {
            oldChildEntry.setOldChildEntry(null);
            return true;
        }

        // Find index of current node in parent
        int currIndex = 0;
        // Iterate until we find the index of node in parent's children
        while (currIndex < parent.n && !parent.children[currIndex].equals(node)) {
            currIndex++;
        }

        // Siblings, null if not exist
        BTreeNode rightSibling = (currIndex + 1 < parent.n) ? parent.children[currIndex + 1] : null;
        BTreeNode leftSibling = (currIndex - 1 >= 0) ? parent.children[currIndex - 1] : null;

        // Try redistribute
        if (rightSibling != null) {
            // Check if right sibling has extra entries
            int total = node.n + rightSibling.n;
            if (total / 2 >= this.t) {
                // Redistribute evenly between right sibling and current node
                while (node.n < total / 2) {
                    // Current node gets first right sibling key
                    node.keys[node.n] = rightSibling.keys[0];
                    node.values[node.n] = rightSibling.values[0];
                    node.n++;
                    // Parent node gets right sibling key
                    parent.keys[currIndex] = rightSibling.keys[1];
                    // Shift right sibling
                    for (int i = 0; i < rightSibling.n - 1; i++) {
                        rightSibling.keys[i] = rightSibling.keys[i + 1];
                    }
                    // Empty last element
                    rightSibling.keys[rightSibling.n - 1] = 0;
                    // Decrement right sibling n
                    rightSibling.n--;
                }
                oldChildEntry.setOldChildEntry(null); // Delete does not go further
                return true;
            }
        }
        if (leftSibling != null) {
            // Check if left sibling has extra entries
            int total = node.n + leftSibling.n;
            if (total / 2 >= this.t) {
                // Redistribute evenly between right sibling and current node through parent
                while (node.n < total / 2) {
                    // Current node gets parent key
                    for (int i = node.n; i > 0; i--) {
                        node.keys[i] = node.keys[i - 1];
                    }
                    node.keys[0] = leftSibling.keys[leftSibling.n - 1];
                    node.values[0] = leftSibling.values[leftSibling.n - 1];
                    node.n++;
                    // Parent node gets left sibling key
                    parent.keys[currIndex - 1] = node.keys[0];
                    // Remove last key value in left sibling
                    leftSibling.keys[leftSibling.n - 1] = 0;
                    leftSibling.values[leftSibling.n - 1] = 0;
                    // Decrement left sibling n
                    leftSibling.n--;
                }
                oldChildEntry.setOldChildEntry(null); // Delete does not go further
                return true;
            }
        }

        // Try to merge
        if (rightSibling != null) {
            oldChildEntry.setOldChildEntry(rightSibling);
            // Move all entries in right sibling into current node
            for (int i = 0; i < rightSibling.n; i++) {
                node.keys[node.n + i] = rightSibling.keys[i];
                node.values[node.n + i] = rightSibling.values[i];
            }
            node.n += rightSibling.n;
            node.next = rightSibling.next;
            // Right sibling will be automatically discarded when parent reference is removed
            return true;
        }
        if (leftSibling != null) {
            oldChildEntry.setOldChildEntry(node);
            // Move all entries in current node into left sibling
            for (int i = 0; i < node.n; i++) {
                leftSibling.keys[leftSibling.n + i] = node.keys[i];
                leftSibling.values[leftSibling.n + i] = node.values[i];
            }
            leftSibling.n += node.n;
            leftSibling.next = node.next;
            // Current node will be automatically discarded when parent reference is removed
            return true;
        }
        return false;
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
        return deleteHelper(null, this.root, studentId, new OldChildEntry());
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
