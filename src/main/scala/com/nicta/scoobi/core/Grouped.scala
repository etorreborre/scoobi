package com.nicta.scoobi
package core

/**
 * A distributed list of associations.
 *
 * @see [[com.nicta.scoobi.core.Association1]]
 */
sealed trait Grouped[K, V] {
  /**
   * The underlying distributed list.
   */
  val list: DList[Association1[K, V]]

  /**
   * Deconstruct the association to a pair.
   */
  def paired(implicit WK: WireFormat[K], WV: WireFormat[V]): DList[(K, Iterable1[V])] =
    list map (_.paired)

  /** make a Grouped runnable, executing the computation and returning the values */
  def run(implicit configuration: core.ScoobiConfiguration): Seq[Association1[K, V]] = {
    import com.nicta.scoobi.Scoobi._
    list.run
  }

  /**
   * Run a function on the values of the distributed list to produce new values.
   */
  def mapValues[W](f: Iterable1[V] => Iterable1[W])(implicit fk: WireFormat[K], fw: WireFormat[W]): Grouped[K, W] =
    Grouped(list map (_ mapValues f))

  /**
   * Run a function on each value in the distributed list to produce a distributed list with new values. Synonym for `:->`.
   */
  def map[W](f: V => W)(implicit fk: WireFormat[K], fw: WireFormat[W]): Grouped[K, W] =
    Grouped(list map (_ map f))

  /**
   * Run a function on each value in the distributed list to produce a distributed list with new values. Synonym for `:->`.
   */
  def mapa[W, X](f: Association1[K, V] => Association1[W, X])(implicit fk: WireFormat[K], fw: WireFormat[W], fx: WireFormat[X]): Grouped[W, X] =
    Grouped(list map f)

  /**
   * Run a function on each value in the distributed list to produce a distributed list with new values. Synonym for `map`.
   */
  def :->[W](f: V => W)(implicit fk: WireFormat[K], fw: WireFormat[W]): Grouped[K, W] =
    map(f)

  /**
   * Run a function on each key in the distributed list to produce a distributed list with new key. Synonym for `<-:`.
   */
  def mapKeys[L](f: K => L)(implicit fl: WireFormat[L], fv: WireFormat[V]): Grouped[L, V] =
    Grouped(list map (_ mapKey f))

  /**
   * Run a function on each key in the distributed list to produce a distributed list with new key. Synonym for `mapKeys`.
   */
  def <-:[L](f: K => L)(implicit fl: WireFormat[L], fv: WireFormat[V]): Grouped[L, V] =
    mapKeys(f)

  /**
   * The keys of the underlying distributed list.
   */
  def keys(implicit fk: WireFormat[K]): DList[K] =
    list map (_.key)

  /**
   * The values of the underlying distributed list grouped by their key.
   */
  def values(implicit fv: WireFormat[V]): DList[Iterable1[V]] =
    list map (_.values)

  /**
   * The values of the underlying distributed list flattened.
   */
  def valuesF(implicit fv: WireFormat[V]): DList[V] =
    list flatMap (_.values.toIterable)

  /**
   * Map two functions on the keys and values (binary map) of the distributed list.
   */
  def bimap[L, W](f: K => L, g: V => W)(implicit fl: WireFormat[L], fw: WireFormat[W]): Grouped[L, W] =
    Grouped(list map (_ bimap (f, g)))

  def parallelDo[B : WireFormat](dofn: DoFn[Association1[K, V], B]): DList[B] =
    list parallelDo dofn

  def combine(f: (V, V) => V)(implicit wk: WireFormat[K], wv: WireFormat[V]): DList[(K, V)] =
    list combine f

  def materialise: DObject[Iterable[Association1[K, V]]]  =
    list.materialise

  def materialise1: DObject[Iterable1[Association1[K, V]]] =
    list.materialise1

  def filter(p: Association1[K, V] => Boolean): Grouped[K, V] =
    Grouped(list filter p)

  import impl.plan.DListImpl
  import impl.plan.comp.GroupByKey
  import WireFormat.wireFormat

  def groupByKey(implicit wk: WireFormat[K], gpk: Grouping[K], wv: WireFormat[V]): Grouped[K, Iterable1[V]] =
    Grouped(new DListImpl(GroupByKey(list.getComp, wk, gpk, wv))(wireFormat[Association1[K, Iterable1[V]]]))

  def ++(g: Grouped[K, V]): Grouped[K, V] =
    Grouped(list ++ g.list)
}

object Grouped {
  /**
   * Construct a `Grouped` with the given distributed list.
   */
  def apply[K, V](x: DList[Association1[K, V]]): Grouped[K, V] =
    new Grouped[K, V] {
      val list = x
    }
}
