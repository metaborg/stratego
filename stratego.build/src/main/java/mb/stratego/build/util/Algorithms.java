package mb.stratego.build.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Algorithms {
    /**
     * The topologically sorted list of strongly connected components of the control-flow graph. We sort the SCCs
     * internally to have a reverse postorder within the component. The basic algorithm is the SCC algorithm of
     * Tarjan (1972).
     *
     * @return The topologically sorted list of strongly connected components.
     */
    public static <N> Deque<Set<N>> topoSCCs(Collection<N> startNodes, Function<N, ? extends Set<N>> next) {
        int index = 0;
        final HashMap<N, Integer> nodeIndex = new HashMap<>();
        final HashMap<N, Integer> nodeLowlink = new HashMap<>(nodeIndex);
        final Deque<N> sccStack = new ArrayDeque<>();
        final Set<N> stackSet = new HashSet<>();
        final Deque<Set<N>> sccs = new ArrayDeque<>();

        /*
         * Note these deviations: (1) We seed the traversal with the start nodes. (2) We use a deque of SCCs, so be can
         * push to the front of it.
         */
        for(N node : startNodes) {
            // For each start node that hasn't been visited already,
            if(nodeIndex.get(node) == null) {
                // do the recursive strong-connect
                index = sccStrongConnect(next, node, index, nodeIndex, nodeLowlink, sccStack, stackSet, sccs::addLast);
            }
        }

        return sccs;
    }

    /**
     * Recursively (DFS) walk the graph and give nodes an index. The lowlink is the lowest index of a node that it can
     * reach *through the DFS*. Therefore once those numbers are propagated, you can find an SCC by finding all nodes
     * with the same lowlink value. Given the way the algorithm works, when on the way back from the DFS you find a node
     * which still has the same index and lowlink value, this can be considered the root of an SCC, and all the nodes
     * above it on the stack are also in that SCC. So you can simply pop nodes of the stack that was kept while doing
     * the DFS (without inspecting them), until you find this node with the same values. As an adaption we also order
     * the nodes in the SCC. We add visited nodes to the stack in post-order even though the set of things on the stack
     * is kept in pre-order. This allows us to very easily give the nodes within an SCC in reverse-postorder
     *
     * @param from        The node to start from
     * @param index       The index to start from
     * @param nodeIndex   The mapping from node to index
     * @param nodeLowlink The mapping from node to lowest index reachable from this node
     * @param sccStack    A stack of nodes being visited during the DFS (*not* used _for_ the DFS, it's recursive not iterative)
     * @param stackSet    The set of nodes on the stack for easier checking if something's on the stack
     * @param addSCC      Add the given scc to the list of SCCs
     * @return The new index value
     */
    private static <N> int sccStrongConnect(Function<N, ? extends Set<N>> next, N from, int index,
        HashMap<N, Integer> nodeIndex, HashMap<N, Integer> nodeLowlink, Deque<N> sccStack, Set<N> stackSet,
        Consumer<Set<N>> addSCC) {
        nodeIndex.put(from, index);
        nodeLowlink.put(from, index);
        index++;

        // Note that we don't actually add the node to the stack, we just say it's on there with this set
        int stackSetSizeBefore = stackSet.size();
        stackSet.add(from);

        for(N to : next.apply(from)) {
            if(nodeIndex.get(to) == null) {
                // Visit neighbours without an index. Propagate lowlink values backward.
                index = sccStrongConnect(next, to, index, nodeIndex, nodeLowlink, sccStack, stackSet, addSCC);
                nodeLowlink.put(from, Integer.min(nodeLowlink.get(from), nodeLowlink.get(to)));
            } else if(stackSet.contains(to)) {
                /*
                 * Neighbours already in the stack are higher in the DFS spanning tree, so we use their index, not their
                 * lowlink. Using the lowlink doesn't break the algorithm, but doesn't help and makes the lowlink have a
                 * less predictable value which cannot be given a clear meaning.
                 */
                nodeLowlink.put(from, Integer.min(nodeLowlink.get(from), nodeIndex.get(to)));
            }
        }

        // Here we actually add the node to the stack, in _postorder_
        sccStack.push(from);

        if(Objects.equals(nodeLowlink.get(from), nodeIndex.get(from))) {
            // Pop the SCC of the stack; since it's a stack, we get a reverse postorder
            java.util.LinkedHashSet<N> scc = new LinkedHashSet<>(2 * (stackSet.size() - stackSetSizeBefore));
            for(int i = stackSet.size(); i > stackSetSizeBefore; i--) {
                N node = sccStack.pop();
                stackSet.remove(node);
                scc.add(node);
            }
            addSCC.accept(Collections.unmodifiableSet(scc));
        }

        return index;
    }
}
