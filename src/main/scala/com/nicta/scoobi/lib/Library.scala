package com.nicta.scoobi
package lib

import core.{DList, Grouping, WireFormat, Grouped, Iterable1, Association1}
import org.apache.hadoop.io._

trait Library {
  /* lib stuff */

  implicit def dlistToRelational[K: WireFormat: Grouping, A: WireFormat](dl: DList[(K, A)]): com.nicta.scoobi.lib.Relational[K,A] = com.nicta.scoobi.lib.Relational(dl)
  implicit def relationalToDList[K, A](r: com.nicta.scoobi.lib.Relational[K, A]): DList[(K,A)] = r.left

  import com.nicta.scoobi.lib.DVector
  import com.nicta.scoobi.lib.InMemDenseVector
  import com.nicta.scoobi.lib.DRowWiseMatrix
  import com.nicta.scoobi.lib.DColWiseMatrix
  import com.nicta.scoobi.lib.InMemVector
  import com.nicta.scoobi.lib.DMatrix

  implicit def dlistToDVector[Elem: WireFormat: Ordering, V: WireFormat: Ordering](v: DList[(Elem, V)]) = DVector(v)
  implicit def dvectorToDList[Elem, V](v: DVector[Elem, V]) = v.data

  implicit def inMemDenseVectorToDObject[T](in: InMemDenseVector[T]) = in.data

  /**
   * Note this is an expensive conversion (it adds an extra map-reduce job), try save the result to reuse if applicable
   */
  implicit def dlistToRowWiseWithMapReduceJob[E : WireFormat : Ordering, T : WireFormat](m: DMatrix[E, T]): DRowWiseMatrix[E, T] =
    DRowWiseMatrix(m.map { case ((r, c), v) => (r, (c, v)) }.groupByKey)

  implicit def dlistToRowWise[Elem: WireFormat: Ordering, T: WireFormat](m: DList[(Elem, Iterable1[(Elem, T)])]): DRowWiseMatrix[Elem, T] =
    DRowWiseMatrix(Grouped(m.map { case (k, v) => Association1(k, v) }))

  implicit def rowWiseToDList[Elem: WireFormat: Ordering, T: WireFormat](m: DRowWiseMatrix[Elem, T]) = m.data


  implicit def dlistToDMatrix[Elem: WireFormat: Ordering, Value: WireFormat](
                                                                                              v: DList[((Elem, Elem), Value)]): DMatrix[Elem, Value] =
    DMatrix[Elem, Value](v)

  implicit def dmatrixToDlist[Elem: WireFormat: Ordering, Value: WireFormat](v: DMatrix[Elem, Value]): DList[((Elem, Elem), Value)] = v.data

  /**
   * Note this is an expensive conversion (it adds an extra map-reduce job), try save the result to reuse if applicable.
   */
  implicit def dlistToColWiseWithMapReduceJob[Elem: WireFormat: Ordering, T: WireFormat](m: DMatrix[Elem, T]): DColWiseMatrix[Elem, T] =
    DColWiseMatrix(m.map { case ((r, c), v) => (c, (r, v)) }.groupByKey)

  implicit def dlistToColWise[Elem : WireFormat: Ordering, T : WireFormat](m: DList[(Elem, Iterable1[(Elem, T)])]): DColWiseMatrix[Elem, T] =
    DColWiseMatrix(Grouped(m.map { case (k, v) => Association1(k, v) }))

  implicit def colWiseToDList[Elem : WireFormat: Ordering, T : WireFormat](m: DColWiseMatrix[Elem, T]) = m.data


  implicit def inMemVectorToDObject[Elem, T](in: InMemVector[Elem, T]) = in.data

  /**
   * implicit conversions to Writables
   */
  implicit def toBooleanWritable(bool: Boolean): BooleanWritable = new BooleanWritable(bool)

  implicit def toIntWritable(int: Int): IntWritable = new IntWritable(int)

  implicit def toFloatWritable(float: Float): FloatWritable = new FloatWritable(float)

  implicit def toLongWritable(long: Long): LongWritable = new LongWritable(long)

  implicit def toDoubleWritable(double: Double): DoubleWritable = new DoubleWritable(double)

  implicit def toText(str: String): Text = new Text(str)

  implicit def toByteWritable(byte: Byte): ByteWritable = new ByteWritable(byte)

  implicit def toBytesWritable(byteArr: Array[Byte]): BytesWritable = new BytesWritable(byteArr)
}


object Library extends Library