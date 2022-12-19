package net.github.douwevos.justflat.startstop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.github.douwevos.justflat.types.values.StartStop;

public class StartStopLine {
	private static final StartStop[] EMPTY_START_STOPS = new StartStop[0];

	public StartStop startStops[];

	public StartStopLine() {
		startStops = EMPTY_START_STOPS;
	}

	
	public StartStopLine(List<StartStop> startStops) {
		if (startStops == null || startStops.isEmpty()) {
			this.startStops = EMPTY_START_STOPS;
		} else {
			this.startStops = new StartStop[startStops.size()];
			startStops.toArray(this.startStops);
		}
	}

	public boolean testDot(long x) {
		if (startStops.length == 0) {
			return false;
		}
		for(StartStop startStop : startStops) {
			if (x>=startStop.start && x<=startStop.stop) {
				return true;
			}
			if (x<startStop.start) {
				break;
			}
		}
		return false;
	}

	
	public StartStopLine duplicate() {
		StartStopLine copy = new StartStopLine();
		copy.startStops = new StartStop[startStops.length];
		System.arraycopy(startStops, 0, copy.startStops, 0, startStops.length);
//		copy.rawDots = rawDots == null ? null :  new ArrayList<>(rawDots);
		return copy;
	}
	
	public void exclude(StartStopLine layerLine) {
		for(StartStop ss : layerLine.startStops) {
			cut(ss.start, ss.stop);
		}
	}
	
	public void edge(StartStopLine linePre, StartStopLine linePost) {
		
		List<StartStop> out = new ArrayList<>();
		
		linePre = linePre.duplicate();
		linePost = linePost.duplicate();

		List<StartStop> linePreEdge = new ArrayList<>();
		List<StartStop> linePostEdge = new ArrayList<>();
		
		for(StartStop ss : startStops) {
//			System.out.println("------------- "+ss);
			
			edgeStartStop(linePre, linePreEdge, ss);
			edgeStartStop(linePost, linePostEdge, ss);
//			System.out.println(""+linePreEdge);
			
			Iterator<StartStop> preIterator = linePreEdge.iterator();
			Iterator<StartStop> postIterator = linePostEdge.iterator();
			StartStop pre = preIterator.hasNext() ? preIterator.next() : null;
			StartStop post = postIterator.hasNext() ? postIterator.next() : null;

			
//			System.out.println("###########");

			int startInsertIndex = out.size();
			out.add(new StartStop(ss.start, ss.start));
			
			while(pre!=null || post!=null) {
//				System.out.println("pre="+pre+" post="+post);
				if (pre!=null) {
					if (post!=null) {
						if (post.stop<pre.start) {
//							System.out.println("  adding post");
							out.add(post);
							post = postIterator.hasNext() ? postIterator.next() : null;
						} else if (pre.stop<post.stop) {
//							System.out.println("  adding pre");
							out.add(pre);
							pre = preIterator.hasNext() ? preIterator.next() : null;
						} else {
							if (pre.start<=post.start) {
								if (post.stop>pre.stop) {
									pre = pre.withStop(post.stop);
								}
								post = postIterator.hasNext() ? postIterator.next() : null;
//								System.out.println("  next post");
							} else {
								if (pre.stop>post.stop) {
									post = post.withStop(pre.stop);
								}
								pre = preIterator.hasNext() ? preIterator.next() : null;
//								System.out.println("  next pre");
							}
							
						}
					} else {
//						System.out.println("  adding pre");
						out.add(pre);
						pre = preIterator.hasNext() ? preIterator.next() : null;
					}
				} else if (post!=null) {
//					System.out.println("  adding post");
					out.add(post);
					post = postIterator.hasNext() ? postIterator.next() : null;
				}
			}

			StartStop last = out.get(out.size()-1);
			if (last.stop!=ss.stop) {
				out.add(new StartStop(ss.stop, ss.stop));
			}
			
			// Remove additional start if it was added double
			StartStop postStart = out.get(startInsertIndex+1);
			if (postStart.start == ss.start) {
				out.remove(startInsertIndex);
			}
		}
		
		if (out.size() == 0) {
			startStops = EMPTY_START_STOPS;
			return;
		}
		
		startStops = new StartStop[out.size()];
		out.toArray(startStops);
	}

	
	private void edgeStartStop(StartStopLine sourceLine, List<StartStop> cropped, StartStop ss) {
		cropped.clear();
		StartStop crop = new StartStop(ss);
//		System.out.println("to crop="+crop);
		for(StartStop source : sourceLine.startStops) {
//			System.out.println("  source="+source+", crop="+crop);
			
			if (source.start>=crop.stop) {
				break;
			}
			if ((source.stop<=crop.start) || (source.start>=crop.stop)) {
				continue;
			}
			if (crop.start<source.start) {
				cropped.add(new StartStop(crop.start, source.start-1));
				if (source.stop>=crop.stop) {
					crop = null;
					break;
				}
			}
			
			if (crop.start<source.stop) {
				if (source.stop>=crop.stop) {
					crop = null;
					break;
				}
				crop = crop.withStart(source.stop+1);
			}
		}
		if (crop!=null) {
			cropped.add(crop);
		}
//		System.out.println("cropped="+cropped);
	}

	


	public long cut(long left, long right) {
		long cutCount = 0;
		for(int i=0; i<startStops.length; i++) {
			StartStop startStop = startStops[i];
			long tleft = startStop.start;
			long tright= startStop.stop;
			
			long hitLeft = tleft<left ? left : tleft;
			long hitRight = tright>right ? right : tright;
			long hitCount = 1+hitRight-hitLeft;
			if (hitCount>0) {
				cutCount += hitCount;
					
				// toggle:  ********
				// to-cut:     **
				if ((left>tleft) && right<tright) {
					startStops[i] = startStop.withStop(left-1);
					i++;
					StartStop copy[] = new StartStop[startStops.length+1];
					System.arraycopy(startStops, 0, copy, 0, i);
					System.arraycopy(startStops, i, copy, i+1, startStops.length-i);
					startStops = copy;
					startStops[i] = new StartStop(right+1, tright);
					break;
				}

				// toggle:  ********
				// to-cut:        ****
				if ((left>tleft) && right>=tright) {
					startStops[i] = startStop.withStop(left-1);
					if (right==tright) {
						break;
					}
				}
					
				// toggle:    ********
				// to-cut:   ****
				if ((left<=tleft) && right<tright) {
					startStops[i] = startStop.withStart(right+1);
				}
					

				// toggle:    ********
				
				// to-cut:   ***********tLeftRight(int at, OnOffDot left, OnOffDot right) {
//				ensureCapacity(size+2);
//				
//				if (at<size) {
//					System.arraycopy(dots, at, dots, at+2, size-at);
//				}
//				dots[at] = left;
//				dots[at+1] = right;
//				size += 2;
//			}


				if ((left<=tleft) && right>=tright) {

					StartStop copy[] = new StartStop[startStops.length-1];
					System.arraycopy(startStops, 0, copy, 0, i);
					System.arraycopy(startStops, i+1, copy, i, startStops.length-1-i);
					i--;
					startStops = copy;
				}
				
			}
		}
		return cutCount;
	}

	


	public void merge(StartStopLine mergeLine) {
		List<StartStop> shapeStartStop = Arrays.asList(mergeLine.startStops);
		if (shapeStartStop==null || shapeStartStop.isEmpty()) {
			return;
		}
		
		if (startStops.length==0) {
			startStops = new StartStop[shapeStartStop.size()];
			startStops = shapeStartStop.toArray(startStops);
			return;
		}
		
		List<StartStop> mergedList = new ArrayList<>();
		
		int sourceIndex = 0;
		StartStop source = startStops[sourceIndex];
		
		for(StartStop doInvert : shapeStartStop) {
			
			while(sourceIndex<startStops.length) {
				
				// sssss
				//        iiiii
				if (source.stop<doInvert.start) {
					mergedList.add(source);
					sourceIndex++;
					source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
					continue;
				}
				
				//              sssss
				//        iiiii
				if (doInvert.stop<source.start) {
					mergedList.add(doInvert);
					break;
				}

				//        sssss
				//        iiiii

				if (doInvert.start == source.start && doInvert.stop == source.stop) {
					mergedList.add(source);
					sourceIndex++;
					source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
					break;
				}

				if (doInvert.start<source.start) {
					
					if (doInvert.stop==source.stop) {
						mergedList.add(doInvert);
						//          sss
						//        iiiii         ii
						sourceIndex++;
						source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
						break;
					}

					if (source.stop>doInvert.stop) {
						source = new StartStop(doInvert.start, source.stop);
						//           sssss     
						//        iiiii         
						break;
					}


					//        sssss
					//       iiiiiii
					
					
				} else {

					if (doInvert.start==source.start) {
						
						if (doInvert.stop>source.stop) {
							//        sss
							//        iiiii            ii
							doInvert = new StartStop(source.start, doInvert.stop);
							sourceIndex++;
							source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
							continue;
						} else {
							//        sssssss
							//        iiiii               ss
							doInvert = new StartStop(doInvert.start, source.stop);
							break;
						}		
					}


					if (source.stop==doInvert.stop) {
						mergedList.add(source);
						//      sssssss       ss  
						//        iiiii
						sourceIndex++;
						source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
						break;
					}

					if (source.stop>doInvert.stop) {
						//        sssss
						//         iii
						source = new StartStop(doInvert.stop+1, source.stop);
						break;
					}

					//      sssss
					//        iiiii       ss   ii
					
					doInvert = new StartStop(source.start, doInvert.stop);
					sourceIndex++;
					source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
					
				}
				
			}
			
		}
		
		while(source!=null) {
			mergedList.add(source);
			sourceIndex++;
			source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
		}
		
		startStops = new StartStop[mergedList.size()];
		startStops = mergedList.toArray(startStops);
	}

	
	public void invert(List<StartStop> shapeStartStop) {
		if (shapeStartStop==null || shapeStartStop.isEmpty()) {
			return;
		}
		
		if (startStops.length==0) {
			startStops = new StartStop[shapeStartStop.size()];
			startStops = shapeStartStop.toArray(startStops);
			return;
		}
		
		List<StartStop> invertedList = new ArrayList<>();
		
		int sourceIndex = 0;
		StartStop source = startStops[sourceIndex];
		
		for(StartStop doInvert : shapeStartStop) {
			
			while(source != null) {
				
				// sssss
				//        iiiii
				if (source.stop<doInvert.start) {
					invertedList.add(source);
					sourceIndex++;
					source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
					continue;
				}
				
				//              sssss
				//        iiiii
				if (doInvert.stop<source.start) {
					invertedList.add(doInvert);
					doInvert = null;
					break;
				}

				//        sssss
				//        iiiii

				if (doInvert.start == source.start && doInvert.stop == source.stop) {
					sourceIndex++;
					source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
					doInvert = null;
					break;
				}

				if (doInvert.start<source.start) {
					
					invertedList.add(new StartStop(doInvert.start, source.start-1));
					if (doInvert.stop==source.stop) {
						//          sss
						//        iiiii         ii
						sourceIndex++;
						source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
						doInvert = null;
						break;
					}

					if (source.stop>doInvert.stop) {
						source = new StartStop(doInvert.stop+1, source.stop);
						//           sssss     
						//        iiiii         
						doInvert = null;
						break;
					}


					//        sssss
					//       iiiiiii
					
					doInvert = new StartStop(source.stop+1, doInvert.stop);
					sourceIndex++;
					source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
					
				} else {

					if (doInvert.start==source.start) {
						
						if (doInvert.stop>source.stop) {
							//        sss
							//        iiiii            ii
							doInvert = new StartStop(source.stop+1, doInvert.stop);
							sourceIndex++;
							source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
							continue;
						} else {
							//        sssssss
							//        iiiii               ss
							doInvert = new StartStop(doInvert.stop+1, source.stop);
							break;
						}
					}

					invertedList.add(new StartStop(source.start, doInvert.start-1));

					if (source.stop==doInvert.stop) {
						//      sssssss       ss  
						//        iiiii
						sourceIndex++;
						source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
						doInvert = null;
						break;
					}

					if (source.stop>doInvert.stop) {
						//        sssss
						//         iii
						source = new StartStop(doInvert.stop+1, source.stop);
						doInvert = null;
						break;
					}

					//      sssss
					//        iiiii       ss   ii
					
					doInvert = new StartStop(source.stop+1, doInvert.stop);
					sourceIndex++;
					source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
				}
			}
			
			if (doInvert != null) {
				invertedList.add(doInvert);
			}
			
		}
		
		while(source!=null) {
			invertedList.add(source);
			sourceIndex++;
			source = sourceIndex<startStops.length ? startStops[sourceIndex] : null;
		}
		
		
		startStops = new StartStop[invertedList.size()];
		startStops = invertedList.toArray(startStops);		
	}

	
	
	public void fillCollisionInfo(LayerCollisionInfo info, long left, long right) {
		long totalHit = 0;
		for(StartStop startStop : startStops) {
			if (right<startStop.start) {
				break;
			}
			long tleft = startStop.start;
			long tright= startStop.stop;

			long hitLeft = tleft<left ? left : tleft;
			long hitRight = tright>right ? right : tright;
			long hitCount = 1+hitRight-hitLeft;
			if (hitCount>0) {
				info.hitCount += hitCount;
				totalHit += hitCount;
			}
		}
		info.misCount += (right+1-left)-totalHit;
	}

	public boolean doesBreach(long left, long right) {

		for(StartStop startStop : startStops) {
			if (left>startStop.stop) {
				//       iiiiiii
				// ssss
				continue;
			}
			if (left<startStop.start) {
				//       iiiiiii
				//                  ssss

				// iiiiiii
				//      ssss

				// iiiiiii
				//   ssssssssssss

				// iiiiiii
				//   sss

				// iiiiiii
				//     sss
				return true;
			}
			
			if (right<=startStop.stop) {
				//   iiiiiii
				// ssssssssssss

				// iiiiiii
				// ssssssssssss

				//        iiiiiii
				//   ssssssssssss
				return false;
			}



			//   iiiiiii
			// ssss

			// iiiiiii
			// sss




			//          iiiiiii
			//   ssssssssssss

			left = startStop.stop+1;
		}
		return true;
	}

	

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for(int idx=0; idx<startStops.length; idx++) {
			StartStop startStop = startStops[idx];
			if (idx>0) {
				s.append(',');
			}
			s.append(startStop.start).append('-').append(startStop.stop);
			
		}
		return "Line["+s+"]";
	}

	public boolean isEmpty() {
		return startStops.length==0;
	}

	
}